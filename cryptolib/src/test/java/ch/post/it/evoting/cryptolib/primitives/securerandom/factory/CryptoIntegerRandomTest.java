/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

class CryptoIntegerRandomTest {

	private CryptoRandomInteger cryptoIntegerRandom;

	@BeforeEach
	void setUp() {
		cryptoIntegerRandom = new SecureRandomFactory().createIntegerRandom();
	}

	@Test
	void testThatGeneratesUpToTheSpecifiedLengthOfDigits() {
		BigInteger n;
		BigInteger bigInteger;

		for (int i = 1; i < 100; i++) {

			bigInteger = cryptoIntegerRandom.genRandomIntegerByDigits(i);

			n = BigInteger.TEN.pow(i).subtract(BigInteger.ONE);

			assertTrue(n.compareTo(bigInteger) > 0 || n.compareTo(bigInteger) == 0);

			assertTrue(i >= bigInteger.toString().length());
		}
	}

	@Test
	void whenGiveAHighLength() {
		assertThrows(IllegalArgumentException.class,
				() -> cryptoIntegerRandom.genRandomIntegerByDigits(SecureRandomConstants.MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH + 1));
	}

	@Test
	void whenGiveAnIncorrectLength() {
		assertThrows(IllegalArgumentException.class,
				() -> cryptoIntegerRandom.genRandomIntegerByDigits(SecureRandomConstants.MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH - 1));
	}

	@Test
	void testThatGeneratesGivenBitLength() {
		for (int numBits = 1; numBits < 100; numBits++) {
			final BigInteger bigInteger = cryptoIntegerRandom.genRandomIntegerByBits(numBits);
			assertTrue(numBits >= bigInteger.bitLength());
		}
	}

	@Test
	void whenGiveAnIncorrectNumberOfBits() {
		assertThrows(IllegalArgumentException.class, () -> cryptoIntegerRandom.genRandomIntegerByBits(0));
	}

	@Test
	void testThatGeneratesIntegerEqualToZero() {
		BigInteger bi = cryptoIntegerRandom.genRandomIntegerByDigits(1);

		while (!bi.equals(BigInteger.ZERO)) {
			bi = cryptoIntegerRandom.genRandomIntegerByDigits(1);
		}

		assertEquals(BigInteger.ZERO, bi);
	}

	@Test
	void testThatNotGeneratesIntegerWhenLengthIsNull() {
		assertThrows(IllegalArgumentException.class, () -> cryptoIntegerRandom.genRandomIntegerByDigits(0));
	}

	//Swiss Post specific code

	@Test
	void testBoundedIntegerGenerationThrowsOnNullBound() {
		assertThrows(NullPointerException.class, () -> cryptoIntegerRandom.genRandomIntegerUpperBounded(null));
	}

	@Test
	void testBoundedIntegerGenerationThrowsForNegativeBound() {
		final BigInteger upperBound = BigInteger.valueOf(-1);
		assertThrows(IllegalArgumentException.class, () -> cryptoIntegerRandom.genRandomIntegerUpperBounded(upperBound));
	}

	@Test
	void testBoundedIntegerGenerationThrowsForOBound() {
		final BigInteger upperBound = BigInteger.ZERO;
		assertThrows(IllegalArgumentException.class, () -> cryptoIntegerRandom.genRandomIntegerUpperBounded(upperBound));
	}

	//End Swiss Post specific code

	@Test
	void testBoundedIntegerGeneration() {
		final BigInteger randomNumber = cryptoIntegerRandom.genRandomIntegerUpperBounded(BigInteger.ONE);
		assertEquals(BigInteger.ZERO, randomNumber);
	}
}
