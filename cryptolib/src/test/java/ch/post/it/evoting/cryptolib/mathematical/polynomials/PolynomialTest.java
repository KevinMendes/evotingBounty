/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class PolynomialTest {

	@Test
	void failsForNegativeDegrees() {
		assertThrows(IllegalArgumentException.class, () -> new Polynomial(-1, BigInteger.ONE, BigInteger.TEN));
	}

	@Test
	void failsForNullModulus() {
		assertThrows(IllegalArgumentException.class, () -> new Polynomial(5, null, BigInteger.ONE));
	}

	@Test
	void failsForGreaterThanConfiguredDegree() {
		final Polynomial p = new Polynomial(2, BigInteger.ONE, BigInteger.TEN);
		// A polynomial of degree 2 must look like: x^2 + x + c
		// Therefore, trying to set a coefficient to have a degree of 3 must fail
		assertThrows(IllegalArgumentException.class, () -> p.setCoefficient(3, BigInteger.ONE));
	}

	@Test
	void failsForNegativeDegreeWhenSettingCoefficients() {
		final Polynomial p = new Polynomial(0, BigInteger.ONE, BigInteger.TEN);

		assertThrows(IllegalArgumentException.class, () -> p.setCoefficient(-1, BigInteger.ONE));
	}

	@Test
	void returnsTheCorrectDegree() {
		final int degree = 10;

		final Polynomial p = new Polynomial(degree, BigInteger.ZERO, BigInteger.TEN);

		assertEquals(degree, p.getDegree());
	}

	@Test
	void itWorks() {
		// Evaluating f(x) = x^2 + 3x + 7 at x = 2
		final Polynomial p = new Polynomial(2, BigInteger.ONE, BigInteger.valueOf(100));

		p.setCoefficient(0, BigInteger.valueOf(7));
		p.setCoefficient(1, BigInteger.valueOf(3));

		final BigInteger expected = BigInteger.valueOf(17);
		final BigInteger result = p.evaluate(BigInteger.valueOf(2));

		assertEquals(expected, result);
	}

	@Test
	void itWorksMod10() {
		// Evaluating f(x) = x^2 + 3x + 7 at x = 2 with modulus = 10
		final Polynomial p = new Polynomial(2, BigInteger.ONE, BigInteger.TEN);

		p.setCoefficient(0, BigInteger.valueOf(7));
		p.setCoefficient(1, BigInteger.valueOf(3));

		final BigInteger expected = BigInteger.valueOf(7);
		final BigInteger result = p.evaluate(BigInteger.valueOf(2));

		assertEquals(expected, result);
	}
}
