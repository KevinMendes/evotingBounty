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

/**
 * Implementation of an exponent that can be used for mathematical operations defined by the Zp subgroup.
 *
 * <p>Instances of this class are immutable.
 */
@JsonRootName("exponent")
public final class Exponent extends AbstractJsonSerializable {

	private final BigInteger q;

	private final BigInteger value;

	/**
	 * Creates an exponent with the specified Zp subgroup q parameter.
	 *
	 * <p>The value of the exponent should be within the range [0..q-1]. If the value provided is not
	 * within this range, then the value assigned to the exponent will be recalculated as follows:
	 *
	 * <p>{@code value = value mod q}
	 *
	 * @param q     the Zp subgroup q parameter.
	 * @param value the value of the exponent.
	 * @throws GeneralCryptoLibException if the Zp subgroup q parameter is null or zero, or if the value is null.
	 */
	@JsonCreator
	public Exponent(
			@JsonProperty("q")
			final
			BigInteger q,
			@JsonProperty("value")
			final
			BigInteger value) throws GeneralCryptoLibException {
		validateInput(q, value);
		this.q = q;
		this.value = calculateValue(value);
	}

	private static void validateInput(final BigInteger q, final BigInteger value) throws GeneralCryptoLibException {
		Validate.notNull(q, "Zp subgroup q parameter");
		Validate.notNull(value, "Exponent value");
		Validate.notLessThan(q, BigInteger.ONE, "Zp subgroup q parameter", "");
	}

	/**
	 * Retrieves the Zp subgroup q parameter for this exponent.
	 *
	 * @return the Zp subgroup q parameter.
	 */
	@JsonProperty("q")
	public BigInteger getQ() {
		return q;
	}

	/**
	 * Retrieves the value of the exponent.
	 *
	 * @return the value of the exponent.
	 */
	@JsonProperty("value")
	public BigInteger getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		final Exponent other = (Exponent) obj;
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
		return "Exponent [q=" + q + ", value=" + value + "]";
	}

	/**
	 * Calculates the value to set for this {@code Exponent}. An exponent value has to be a number between {@code 0} and {@code q-1} inclusive, so if
	 * the received value is less than {@code 0} or greater than {@code q-1}, {@code mod q} has to be applied.
	 *
	 * @param exponentValue the value of the exponent.
	 * @return the value to set to this exponent.
	 */
	private BigInteger calculateValue(final BigInteger exponentValue) {

		final BigInteger result;
		if ((q.compareTo(exponentValue) > 0) && (BigInteger.ZERO.compareTo(exponentValue) < 1)) {
			result = exponentValue;
		} else {
			result = exponentValue.mod(q);
		}
		return result;
	}

}
