/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import java.math.BigInteger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.mathematical.groups.MathematicalGroup;

/**
 * Utilities to generate random exponents.
 */
public class Exponents {

	private Exponents() {
	}

	/**
	 * Generates a uniformly distributed random Exponent for the received mathematical group.
	 *
	 * <p>The value of the created exponent will be between {@code 0} and {@code q-1}.
	 *
	 * @param group               a mathematical group.
	 * @param cryptoRandomInteger the entropy source used to generate the value of the Exponent.
	 */
	public static Exponent random(final MathematicalGroup<?> group, final CryptoAPIRandomInteger cryptoRandomInteger)
			throws GeneralCryptoLibException {

		Validate.notNull(group, "Zp subgroup");

		final BigInteger value = getRandomExponentValue(group.getQ().bitLength(), group.getQ(), cryptoRandomInteger);
		return new Exponent(group.getQ(), value);
	}

	private static BigInteger getRandomExponentValue(final int bitLength, final BigInteger q, final CryptoAPIRandomInteger cryptoRandomInteger) {

		BigInteger random;

		do {
			random = cryptoRandomInteger.genRandomIntegerByBits(bitLength);
		} while (random.compareTo(q) >= 0);

		return random;
	}
}
