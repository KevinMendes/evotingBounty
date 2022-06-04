/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class LagrangePolynomialTest {

	@Test
	void failsForLessThanTwoPoints() {
		assertThrows(IllegalArgumentException.class, () -> new LagrangePolynomial(Collections.emptyList(), BigInteger.ONE));
	}

	@Test
	void failsForRepeatedXValues() {
		final Point p0 = new Point(BigInteger.ONE, BigInteger.TEN);
		final Point p1 = new Point(BigInteger.ZERO, BigInteger.valueOf(5));
		final Point p2 = new Point(BigInteger.ONE, BigInteger.valueOf(100));

		assertThrows(IllegalArgumentException.class, () -> new LagrangePolynomial(Arrays.asList(p0, p1, p2), BigInteger.TEN));
	}

	@Test
	void isAbleToInterpolate() {
		// Polynomial in question: f(x) = 2 * x^3 + 4
		final List<Point> points = new ArrayList<>(100);
		for (int i = 1; i < 100; i++) {
			points.add(new Point(BigInteger.valueOf(i), BigInteger.valueOf(2 * i * i * i + 4)));
		}

		final LagrangePolynomial p = new LagrangePolynomial(points, BigInteger.valueOf(15_485_863));

		final BigInteger evaluateAtZero = p.evaluateAtZero();

		assertEquals(BigInteger.valueOf(4), evaluateAtZero); // f(0) = 4
	}

	@Test
	void isAbleToInterpolateWithModulus() {
		// Polynomial in question: f(x) = -x^2 - 25x + 24
		final List<Point> points = new ArrayList<>(100);
		for (int i = -50; i < -30; i++) {
			points.add(new Point(BigInteger.valueOf(i), BigInteger.valueOf(-(i * i) - 25 * i + 24)));
		}

		final LagrangePolynomial p = new LagrangePolynomial(points, BigInteger.valueOf(23));

		final BigInteger evaluateAtZero = p.evaluateAtZero();

		assertEquals(BigInteger.ONE, evaluateAtZero); // f(0) mod 23 = 1
	}
}
