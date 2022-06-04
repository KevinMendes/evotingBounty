/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.secretsharing.shamir;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;

/**
 * A share implementation that holds data needed for the shamir scheme part of a secret.
 */
public class ShamirShare implements Share {

	// Number of secrets this set of shares has.
	private final int numberOfSecrets;

	// Number of parts this set of shares has.
	private final int numberOfParts;

	// Threshold this set of shares has.
	private final int threshold;

	// Modulus to calculate the set of shares.
	private final BigInteger modulus;

	// Length of the byte array containing the secret.
	private final int secretLength;

	// The fragment of the secret this share constitutes.
	private final List<Point> points;

	/**
	 * Build the {@link ShamirShare} from its parameters.
	 *
	 * @param numberOfParts Number of fragments of the set of shares this one belongs to.
	 * @param threshold     Threshold of the set of shares this one belongs to.
	 * @param modulus       the modulus for each secret of the set of shares this one belongs to.
	 * @param secretLength  The length of the secret in bytes.
	 * @param points        A list of {@link Point}s. Each point is from a different Lagrange polynomial corresponding to a different secret. The
	 *                      order of the points should be the same across shares, with regards to the secret it corresponds to.
	 */
	public ShamirShare(final int numberOfParts, final int threshold, final BigInteger modulus, final int secretLength, final List<Point> points) {
		numberOfSecrets = points.size();
		this.numberOfParts = numberOfParts;
		this.threshold = threshold;
		this.modulus = modulus;
		this.secretLength = secretLength;
		this.points = points;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		final ShamirShare that = (ShamirShare) obj;

		if (numberOfSecrets != that.getNumberOfSecrets()) {
			return false;
		}
		if (numberOfParts != that.getNumberOfParts()) {
			return false;
		}
		if (threshold != that.getThreshold()) {
			return false;
		}
		if (modulus != null ? !modulus.equals(that.getModulus()) : that.getModulus() != null) {
			return false;
		}

		return points != null ? points.equals(that.getPoints()) : that.getPoints() == null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(numberOfSecrets, numberOfParts, threshold, modulus, points);
	}

	@Override
	public String toString() {
		return "ShamirShare [numberOfSecrets=" + numberOfSecrets + ", numberOfParts=" + numberOfParts + ", threshold=" + threshold + ", moduli="
				+ modulus + ", points=" + points + "]";
	}

	@Override
	public int getNumberOfParts() {
		return numberOfParts;
	}

	@Override
	public int getThreshold() {
		return threshold;
	}

	@Override
	public BigInteger getModulus() {
		return modulus;
	}

	@Override
	public int getSecretLength() {
		return secretLength;
	}

	@Override
	public int getNumberOfSecrets() {
		return numberOfSecrets;
	}

	/**
	 * Get the {@link Point} evaluation of the Lagrange polynomial this {@link Share} stores.
	 *
	 * @return Returns the point.
	 */
	public List<Point> getPoints() {
		return points;
	}
}
