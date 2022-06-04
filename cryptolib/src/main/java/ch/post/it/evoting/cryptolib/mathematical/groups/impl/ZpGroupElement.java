/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.serialization.AbstractJsonSerializable;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers;
import ch.post.it.evoting.cryptolib.mathematical.groups.GroupElement;

/**
 * Class which defines a Zp group element.
 *
 * <p>Instances of this class are immutable.
 */
@JsonRootName("zpGroupElement")
public final class ZpGroupElement extends AbstractJsonSerializable implements GroupElement {

	private final BigInteger value;

	private final BigInteger p;

	private final BigInteger q;

	/**
	 * Creates {@code ZpSubgroupElement}. The specified value should be an element of the group.
	 *
	 * @param value the value of the element. It must be between {@code [1..p-1]}
	 * @param group the {@link ZpSubgroup} to which this element belongs.
	 * @throws GeneralCryptoLibException if the parameters are not valid.
	 */
	public ZpGroupElement(final BigInteger value, final ZpSubgroup group) throws GeneralCryptoLibException {
		this(value, getPFromGroup(group), getQFromGroup(group));
	}

	/**
	 * Constructor which allows the creation of a {@code ZpSubgroupElement} without the need of a ZpSubgroup. The received value should be an element
	 * of the group defined by p and q. For the exact requirements that must be fulfilled by a group member please see the description of the method
	 * {@link ZpSubgroup#isGroupMember(ZpGroupElement)}
	 *
	 * <p>A group has an associated generator, and a generator is associated with a group, therefore,
	 * the question of which to create first (the generator or the group) is not obvious. The intended use of this constructor is to allow the
	 * creation of a group element (which can serve as a generator), without the need of first creating a group. This constructor can therefore be
	 * called by the constructor of a mathematical group, in order to create it's generator, without first having created a mathematical group.
	 *
	 * @param value the value of the element. It must be between {@code [1..p-1]}.
	 * @param p     the ElGamal p parameter (modulus).
	 * @param q     the ElGamal q parameter (order).
	 * @throws GeneralCryptoLibException if parameters are not valid.
	 */
	@JsonCreator
	public ZpGroupElement(
			@JsonProperty("value")
			final
			BigInteger value,
			@JsonProperty("p")
			final
			BigInteger p,
			@JsonProperty("q")
			final
			BigInteger q) throws GeneralCryptoLibException {
		validateInput(value, p, q);
		this.value = value;
		this.p = p;
		this.q = q;
	}

	private static BigInteger getPFromGroup(final ZpSubgroup group) throws GeneralCryptoLibException {
		Validate.notNull(group, "Zp subgroup");
		return group.getP();
	}

	private static BigInteger getQFromGroup(final ZpSubgroup group) throws GeneralCryptoLibException {
		Validate.notNull(group, "Zp subgroup");
		return group.getQ();
	}

	private static void validateInput(final BigInteger value, final BigInteger p, final BigInteger q) throws GeneralCryptoLibException {

		Validate.notNull(value, "Zp group element value");
		Validate.notNull(p, "Zp subgroup p parameter");
		Validate.notNull(q, "Zp subgroup q parameter");
		final BigInteger pMinusOne = p.subtract(BigInteger.ONE);
		Validate.inRange(q, BigInteger.ONE, pMinusOne, "Zp subgroup q parameter", "", "Zp subgroup p parameter minus 1");
		Validate.inRange(value, BigInteger.ONE, pMinusOne, "Zp group element value", "", "Zp subgroup p parameter minus 1");
	}

	/**
	 * In this implementation of {@link GroupElement}, the multiply operation returns ZpSubgroupElement whose value is {@code (this * element) mod
	 * p}.
	 *
	 * @return {@code (this * element) mod p}.
	 * @throws GeneralCryptoLibException if {@code element} is not valid.
	 */
	@Override
	public ZpGroupElement multiply(final GroupElement element) throws GeneralCryptoLibException {
		validateReceivedElementIsFromSameGroup(element);
		final BigInteger result = BigIntegers.modMultiply(value, element.getValue(), p);
		return new ZpGroupElement(result, p, q);
	}

	/**
	 * In this implementation of {@link GroupElement}, the exponentiate operation returns ZpSubgroupElement whose value is {@code
	 * (this<sup>exponent</sup>) mod p}.
	 *
	 * @return {@code (this<sup>exponent</sup>) mod p}.
	 * @throws GeneralCryptoLibException if {@code exponent} is not valid.
	 */
	@Override
	public ZpGroupElement exponentiate(final Exponent exponent) throws GeneralCryptoLibException {
		validateReceivedExponent(exponent);
		final BigInteger valueExponentiated = BigIntegers.modPow(value, exponent.getValue(), p);
		return new ZpGroupElement(valueExponentiated, p, q);
	}

	/**
	 * Retrieves the p parameter of the Zp subgroup to which the Zp group element belongs.
	 *
	 * @return the p parameter of the Zp subgroup.
	 */
	@JsonProperty("p")
	public BigInteger getP() {
		return p;
	}

	/**
	 * Retrieves the q parameter of the Zp subgroup to which the Zp group element belongs.
	 *
	 * @return the q parameter of the Zp subgroup.
	 */
	@JsonProperty("q")
	public BigInteger getQ() {
		return q;
	}

	/**
	 * Retrieves the value of the Zp group element.
	 *
	 * @return the value of the Zp group element.
	 */
	@JsonProperty("value")
	@Override
	public BigInteger getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((q == null) ? 0 : q.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ZpGroupElement other = (ZpGroupElement) obj;
		if (p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!p.equals(other.p)) {
			return false;
		}
		if (q == null) {
			if (other.q != null) {
				return false;
			}
		} else if (!q.equals(other.q)) {
			return false;
		}
		if (value == null) {
			return other.value == null;
		} else {
			return value.equals(other.value);
		}
	}

	@Override
	public String toString() {
		return "ZpGroupElement [value=" + value + ", p=" + p + ", q=" + q + "]";
	}

	private void validateReceivedElementIsFromSameGroup(final GroupElement groupElement) throws GeneralCryptoLibException {

		Validate.notNull(groupElement, "Zp group element");

		final ZpGroupElement zpSubgroupElement;
		if (groupElement instanceof ZpGroupElement) {
			zpSubgroupElement = (ZpGroupElement) groupElement;
		} else {
			throw new GeneralCryptoLibException("This Zp group element is not a member of a Zp subgroup.");
		}

		if (!q.equals(zpSubgroupElement.getQ()) && !p.equals(zpSubgroupElement.getP())) {
			throw new GeneralCryptoLibException("Operations can only be performed on group elements which are members of the same group");
		}
	}

	private void validateReceivedExponent(final Exponent exponent) throws GeneralCryptoLibException {

		Validate.notNull(exponent, "Exponent");
		Validate.notNull(exponent.getValue(), "Exponent value");

		if (!q.equals(exponent.getQ())) {
			throw new GeneralCryptoLibException(
					new IllegalArgumentException("The exponent should be of the same Zp subgroup order as this Zp group element"));
		}
	}
}
