/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.serialization.AbstractJsonSerializable;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers;
import ch.post.it.evoting.cryptolib.mathematical.groups.MathematicalGroup;

/**
 * Given p and Zp representing 'Integers (mod p)' group, a Zp subgroup is a group where the elements are a subset of elements from Zp.
 *
 * <p>The order of the subgroup is q, that is the number of elements. Both Zp and the ZpSubgroup are
 * finite cyclic groups, it means that all elements can be generated exponentiating a special group element called generator.
 *
 * <p>When the p and q are related with the restriction p = 2q + 1 the subgroup is also defined as
 * 'Quadratic Residue'.
 *
 * <p>Instances of this class are immutable.
 */
@JsonRootName("zpSubgroup")
@JsonIgnoreProperties({ "generator", "identity" })
public final class ZpSubgroup extends AbstractJsonSerializable implements MathematicalGroup<ZpGroupElement> {

	private final BigInteger p;

	private final BigInteger q;

	private final BigInteger g;

	private final ZpGroupElement generator;

	private final ZpGroupElement identity;

	/**
	 * Constructs a {@code ZpSubgroup} using provided parameters.
	 *
	 * <p>Note: generator must be a group element different from one. For the exact requirements that
	 * must be fulfilled by a group member please see the description of the method {@link ZpSubgroup#isGroupMember(ZpGroupElement)}
	 *
	 * @param g The generator value of the subgroup. This value must pertain to a group element different from one.
	 * @param p The modulus.
	 * @param q The order of the subgroup.
	 * @throws GeneralCryptoLibException if {@code 0<q<p} restriction is not accomplished.
	 */
	@JsonCreator
	public ZpSubgroup(
			@JsonProperty("g")
			final
			BigInteger g,
			@JsonProperty("p")
			final
			BigInteger p,
			@JsonProperty("q")
			final
			BigInteger q) throws GeneralCryptoLibException {
		validateInput(p, q, g);
		this.p = p;
		this.q = q;
		this.g = g;
		generator = new ZpGroupElement(this.g, this.p, this.q);
		identity = new ZpGroupElement(BigInteger.ONE, this);
	}

	/**
	 * Deserializes the instance from a string in JSON format.
	 *
	 * @param json the JSON
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	public static ZpSubgroup fromJson(final String json) throws GeneralCryptoLibException {
		return AbstractJsonSerializable.fromJson(json, ZpSubgroup.class);
	}

	private static void validateInput(final BigInteger p, final BigInteger q, final BigInteger g) throws GeneralCryptoLibException {
		Validate.notNull(p, "Zp subgroup p parameter");
		Validate.notNull(q, "Zp subgroup q parameter");
		Validate.notNull(g, "Zp subgroup generator");
		final BigInteger pMinusOne = p.subtract(BigInteger.ONE);
		Validate.inRange(q, BigInteger.ONE, pMinusOne, "Zp subgroup q parameter", "", "Zp subgroup p parameter minus 1");
		Validate.inRange(g, BigInteger.valueOf(2), pMinusOne, "Zp subgroup generator", "", "Zp subgroup p parameter minus 1");
	}

	/**
	 * Checks if a value is a member of this group. A given value is a member of this group if:
	 *
	 * <ul>
	 *   <li>The given value is an integer between {@code 1} and {@code p-1}: {@code (0 < value < p)}
	 *   <li>{@code (value<sup>q</sup> mod p) = 1}
	 * </ul>
	 */
	public boolean isGroupMember(final BigInteger value) {
		final BigInteger modPow = BigIntegers.modPow(value, q, p);
		return BigInteger.ONE.equals(modPow);
	}

	/**
	 * Checks if an {@code element} is a member of this group. For this implementation of {@link MathematicalGroup}, a given element is a member of
	 * this group if:
	 *
	 * <ul>
	 *   <li>The given element has an integer value between {@code 1} and {@code p-1}: {@code (0 <
	 *       element < p)}
	 *   <li>{@code (this<sup>q</sup> mod p) = 1}
	 * </ul>
	 */
	@Override
	public boolean isGroupMember(final ZpGroupElement element) {

		// The check that the element has an integer value between 1 and p-1 is
		// done in the element constructor, so it is not necessary to do it here
		// again

		if (!element.getP().equals(p)) {
			return false;
		}

		return isGroupMember(element.getValue());
	}

	@Override
	@JsonProperty("p")
	public BigInteger getP() {
		return p;
	}

	@Override
	@JsonProperty("q")
	public BigInteger getQ() {
		return q;
	}

	@Override
	@JsonProperty("g")
	public BigInteger getG() {
		return g;
	}

	@Override
	public ZpGroupElement getGenerator() {
		return generator;
	}

	@Override
	public ZpGroupElement getIdentity() {
		return identity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((generator == null) ? 0 : generator.hashCode());
		result = prime * result + ((p == null) ? 0 : p.hashCode());
		result = prime * result + ((q == null) ? 0 : q.hashCode());
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
		final ZpSubgroup other = (ZpSubgroup) obj;
		if (generator == null) {
			if (other.generator != null) {
				return false;
			}
		} else if (!generator.equals(other.generator)) {
			return false;
		}
		if (p == null) {
			if (other.p != null) {
				return false;
			}
		} else if (!p.equals(other.p)) {
			return false;
		}
		if (q == null) {
			return other.q == null;
		} else {
			return q.equals(other.q);
		}
	}
}
