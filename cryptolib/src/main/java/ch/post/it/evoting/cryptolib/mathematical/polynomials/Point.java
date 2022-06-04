/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import java.math.BigInteger;
import java.util.Objects;

/**
 * This type represents an interpolation point of the polynomial, i.e., a pair of the form (x, P(x)) for a given x.
 */
public final class Point {

	private final BigInteger x;

	private final BigInteger y;

	/**
	 * A Point defined by the coordinates x and y.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 */
	public Point(final BigInteger x, final BigInteger y) {
		if (x == null) {
			throw new IllegalArgumentException("The x-coordinate cannot be null.");
		}

		if (y == null) {
			throw new IllegalArgumentException("The y-coordinate cannot be null.");
		}

		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the x-coordinate of this point.
	 *
	 * @return a {@link BigInteger} which holds the x-value.
	 */
	public BigInteger getX() {
		return x;
	}

	/**
	 * Returns the y-coordinate of this point.
	 *
	 * @return a {@link BigInteger} which holds the y-value.
	 */
	public BigInteger getY() {
		return y;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final Point that = (Point) obj;

		if (x != null ? !x.equals(that.getX()) : that.getX() != null) {
			return false;
		}

		return y != null ? y.equals(that.getY()) : that.getY() == null;
	}

	@Override
	public String toString() {
		return String.format("Point [x=%s, y=%s]", x, y);
	}
}
