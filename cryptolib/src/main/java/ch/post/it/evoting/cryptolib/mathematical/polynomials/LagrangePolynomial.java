/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers;

/**
 * This class represents a polynomial P specified by a set of (x_i, P(x_i)) pairs. The value of the polynomial at any other point is then evaluated
 * using the Lagrange interpolation formula.
 */
public final class LagrangePolynomial {

	private final List<Point> points;

	private final BigInteger modulus;

	/**
	 * Constructs a polynomial using a list of (x, P(x)) values.
	 *
	 * @param pointList list of interpolation values.
	 * @param modulus   modulus for operation between points.
	 */
	public LagrangePolynomial(final List<Point> pointList, final BigInteger modulus) {
		if (pointList.size() < 2) {
			throw new IllegalArgumentException("The interpolation values list must have at least two points.");
		}

		points = new ArrayList<>(pointList.size());
		final Set<BigInteger> usedXPoints = new HashSet<>();

		pointList.forEach(p -> {
			if (usedXPoints.contains(p.getX())) {
				throw new IllegalArgumentException("There are repeated x-values in the given list of interpolation values.");
			}
			points.add(p);
			usedXPoints.add(p.getX());
		});

		this.modulus = modulus;
	}

	/**
	 * Evaluates this polynomial at zero.
	 *
	 * @return the result of the evaluation.
	 */
	public BigInteger evaluateAtZero() {
		final BigInteger[] coefficients = new BigInteger[points.size()];

		for (int i = 0; i < points.size(); i++) {
			coefficients[i] = BigInteger.ONE;

			// Compute the Lagrange basis polynomials evaluated at zero
			for (int j = 0; j < points.size(); j++) {
				if (i != j) {
					final BigInteger dividend = points.get(j).getX();
					final BigInteger divisor = points.get(j).getX().subtract(points.get(i).getX()).mod(modulus);

					coefficients[i] = BigIntegers
							.modMultiply(coefficients[i], BigIntegers.modMultiply(dividend, divisor.modInverse(modulus), modulus), modulus);
				}
			}
		}

		// Interpolate the polynomial at zero using the input points
		BigInteger result = BigInteger.ZERO;
		for (int i = 0; i < points.size(); i++) {
			result = result.add(BigIntegers.modMultiply(coefficients[i], points.get(i).getY(), modulus)).mod(modulus);
		}

		return result;
	}

	/**
	 * @return Returns the points.
	 */
	public List<Point> getPoints() {
		return new ArrayList<>(points);
	}

	/**
	 * @return Returns the modulus.
	 */
	public BigInteger getModulus() {
		return modulus;
	}
}
