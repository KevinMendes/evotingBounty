/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.bigintegers;

import static ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers.modMultiply;
import static ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers.modPow;
import static ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers.multiply;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class BigIntegersTest {

	private static final BigInteger THREE = BigInteger.valueOf(3);
	private static final BigInteger FIVE = BigInteger.valueOf(5);
	private static final BigInteger SEVEN = BigInteger.valueOf(7);
	private static final BigInteger ELEVEN = BigInteger.valueOf(11);

	@Test
	void testModMultiply() {
		assertEquals(THREE.multiply(FIVE).mod(SEVEN), modMultiply(THREE, FIVE, SEVEN));
	}

	@Test
	void testModMultiplyNegative() {
		assertEquals(THREE.multiply(FIVE.negate()).mod(SEVEN), modMultiply(THREE, FIVE.negate(), SEVEN));
	}

	@Test
	void testModPow() {
		final BigInteger minusOne = BigInteger.ONE.negate();

		// Base 	Exponent 	modulus 	Expected result of modPow operation
		// 0 	0 	7 	1
		runModPowTest(BigInteger.ZERO, BigInteger.ZERO, SEVEN);
		// 0 	0 	11 	1
		runModPowTest(BigInteger.ZERO, BigInteger.ZERO, ELEVEN);
		// 0 	1 	7 	0
		runModPowTest(BigInteger.ZERO, BigInteger.ONE, SEVEN);
		// 0 	1 	11 	0
		runModPowTest(BigInteger.ZERO, BigInteger.ONE, ELEVEN);
		// 0 	7 	7 	0
		runModPowTest(BigInteger.ZERO, SEVEN, SEVEN);
		// 0 	7 	11 	0
		runModPowTest(BigInteger.ZERO, SEVEN, ELEVEN);
		// 0 	11 	7 	0
		runModPowTest(BigInteger.ZERO, ELEVEN, SEVEN);
		// 0 	11 	11 	0
		runModPowTest(BigInteger.ZERO, ELEVEN, ELEVEN);
		// 1 	0 	7 	1
		runModPowTest(BigInteger.ONE, BigInteger.ZERO, SEVEN);
		// 1 	0 	11 	1
		runModPowTest(BigInteger.ONE, BigInteger.ZERO, ELEVEN);
		// 1 	1 	7 	1
		runModPowTest(BigInteger.ONE, BigInteger.ONE, SEVEN);
		// 1 	1 	11 	1
		runModPowTest(BigInteger.ONE, BigInteger.ONE, ELEVEN);
		// 1 	-1 	7 	1
		runModPowTest(BigInteger.ONE, minusOne, SEVEN);
		// 1 	-1 	11 	1
		runModPowTest(BigInteger.ONE, minusOne, ELEVEN);
		// 1 	7 	7 	1
		runModPowTest(BigInteger.ONE, SEVEN, SEVEN);
		// 1 	7 	11 	1
		runModPowTest(BigInteger.ONE, SEVEN, ELEVEN);
		// 1 	11 	7 	1
		runModPowTest(BigInteger.ONE, ELEVEN, SEVEN);
		// 1 	11 	11 	1
		runModPowTest(BigInteger.ONE, ELEVEN, ELEVEN);
		// -1 	0 	7 	1
		runModPowTest(minusOne, BigInteger.ZERO, SEVEN);
		// -1 	0 	11 	1
		runModPowTest(minusOne, BigInteger.ZERO, ELEVEN);
		// -1 	1 	7 	6
		runModPowTest(minusOne, BigInteger.ONE, SEVEN);
		// -1 	1 	11 	10
		runModPowTest(minusOne, BigInteger.ONE, ELEVEN);
		// -1 	-1 	7 	6
		runModPowTest(minusOne, minusOne, SEVEN);
		// -1 	-1 	11 	10
		runModPowTest(minusOne, minusOne, ELEVEN);
		// -1 	7 	7 	6
		runModPowTest(minusOne, SEVEN, SEVEN);
		// -1 	7 	11 	10
		runModPowTest(minusOne, SEVEN, ELEVEN);
		// -1 	11 	7 	6
		runModPowTest(minusOne, ELEVEN, SEVEN);
		// -1 	11 	11 	10
		runModPowTest(minusOne, ELEVEN, ELEVEN);
	}

	@Test
	void testModPowArithmeticException() {
		final BigInteger oneNegate = BigInteger.ONE.negate();
		assertThrows(ArithmeticException.class, () -> modPow(BigInteger.ZERO, oneNegate, ELEVEN));
	}

	private void runModPowTest(final BigInteger base, final BigInteger exponent, final BigInteger modulus) {
		final String message = String.format("BigIntegers.modPow diverges from Java for: %s ^ %s mod %s", base, exponent, modulus);
		assertEquals(base.modPow(exponent, modulus), modPow(base, exponent, modulus), message);
	}

	@Test
	void testMultiply() {
		assertEquals(THREE.multiply(FIVE), multiply(THREE, FIVE));
	}

}
