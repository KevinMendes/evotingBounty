/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import java.math.BigInteger;

import ch.post.it.evoting.cryptolib.mathematical.bigintegers.BigIntegers;

/**
 * Representation of a single-indeterminate polynomial as a set of its coefficients, and a modulus that applies to all operations.
 */
public final class Polynomial {

	/**
	 * The vector containing the polynomial coefficients.
	 */
	private final BigInteger[] cl;

	/**
	 * The modulus of the polynomial
	 */
	private final BigInteger modulus;

	/**
	 * Creates a new Polynomial of the given degree, with all its coefficients set to the given value.
	 *
	 * @param degree  the degree of the Polynomial.
	 * @param value   the default value for all coefficients.
	 * @param modulus Operations adding and multiplying elements will be modulus this value.
	 */
	public Polynomial(final int degree, final BigInteger value, final BigInteger modulus) {
		if (degree < 0) {
			throw new IllegalArgumentException("A polynomial cannot have a negative degree.");
		}

		if (value == null) {
			throw new IllegalArgumentException("The default value for the coefficients cannot be null.");
		}

		final int elements = degree + 1;
		cl = new BigInteger[elements];
		final BigInteger defaultValue = value.mod(modulus);

		for (int i = 0; i < elements; i++) {
			cl[i] = defaultValue;
		}

		this.modulus = modulus;
	}

	/**
	 * Sets a coefficient value.
	 *
	 * @param deg   the degree of the coefficient.
	 * @param value the new coefficient's value.
	 */
	public void setCoefficient(final int deg, final BigInteger value) {
		if (deg < 0) {
			throw new IllegalArgumentException("Cannot set a coefficient with a negative degree.");
		}

		if (deg >= cl.length) {
			throw new IllegalArgumentException(
					String.format("The degree of the coefficient (%d) is invalid, this polynomial has a degree of %d.", deg, cl.length - 1));
		}

		cl[deg] = value;
	}

	/**
	 * Evaluates this polynomial at the given point.
	 *
	 * @param point the point
	 * @return evaluation of this polynomial at given point
	 */
	public BigInteger evaluate(final BigInteger point) {
		BigInteger result;

		// Evaluate using Horner's method.
		int i = cl.length - 1;
		result = cl[i];
		i--;
		for (; i >= 0; --i) {
			result = BigIntegers.modMultiply(result, point, modulus);
			result = result.add(cl[i]).mod(modulus);
		}

		return result;
	}

	public int getDegree() {
		return cl.length - 1;
	}
}
