/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Tests for {@link Exponents}.
 */
class ExponentsTest extends ExponentTestBase {

	@Test
	void givenNullGroupAndCryptoSecureRandomWhenAttemptToCreateExponentThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> Exponents.random(null, _cryptoRandomInteger));
	}

	@Test
	void testWhenRandomExponentCreatedThenValueIsInRange() throws GeneralCryptoLibException {

		boolean notLessThanZero = false;
		boolean lessThanQ = false;

		final Exponent randomExponent = Exponents.random(_smallGroup, _cryptoRandomInteger);

		if (BigInteger.ZERO.compareTo(randomExponent.getValue()) < 1) {
			notLessThanZero = true;
		}
		if (randomExponent.getValue().compareTo(_smallQ) < 0) {
			lessThanQ = true;
		}

		assertTrue(notLessThanZero, "The random exponent should be equal or greater than zero");
		assertTrue(lessThanQ, "The random exponent should be less than q");
	}

	@Test
	void testRandomExponents() throws GeneralCryptoLibException {
		final String errorMessage = "The random exponents should be different";

		final Exponent exponent1 = Exponents.random(_largeGroup, _cryptoRandomInteger);
		final Exponent exponent2 = Exponents.random(_largeGroup, _cryptoRandomInteger);
		final Exponent exponent3 = Exponents.random(_largeGroup, _cryptoRandomInteger);

		assertNotEquals(exponent1.getValue(), exponent2.getValue(), errorMessage);
		assertNotEquals(exponent1.getValue(), exponent3.getValue(), errorMessage);
		assertNotEquals(exponent2.getValue(), exponent3.getValue(), errorMessage);
	}
}
