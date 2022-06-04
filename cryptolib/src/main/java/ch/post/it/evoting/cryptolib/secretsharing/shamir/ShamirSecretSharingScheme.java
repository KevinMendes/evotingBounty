/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.secretsharing.shamir;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingSchemeAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.LagrangePolynomial;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Polynomial;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

/**
 * Implementation of the Shamir secret share scheme.
 */
public final class ShamirSecretSharingScheme implements ThresholdSecretSharingSchemeAPI {

	private static LagrangePolynomial splitSecret(final BigInteger secret, final int numberOfShares, final int threshold, final BigInteger modulus) {

		final Polynomial gp = new Polynomial(threshold - 1, BigInteger.ZERO, modulus);
		final BigInteger m = modulus.subtract(BigInteger.ONE);

		// How to achieve uniform distribution of coefficients between 0 and
		// modulus-1. For each coefficient:
		// 1. Generate the coefficient as a random number of the bit length of
		// the value modulus-1
		// 2. Is the coefficient smaller than or equal to modulus-1?
		// 2.1. YES, OK and proceed to generate next coefficient
		// 2.2. NO, discard the generated and do it again.
		// Public evaluation point for the secret is zero
		gp.setCoefficient(0, secret);

		// Create CryptoRandInteger class to get next random BigInteger.
		final CryptoAPIRandomInteger cryptoRandomInteger = new PrimitivesService().getCryptoRandomInteger();

		for (int i = 1; i < threshold; ++i) {
			gp.setCoefficient(i, cryptoRandomInteger.genRandomIntegerUpperBounded(m));
		}

		final Point[] points = new Point[numberOfShares];
		// Generate the shadow values using consecutive values
		// Public evaluation point of j-th share is j for j=1,...,numberOfShares
		for (int j = 1; j <= numberOfShares; ++j) {
			final BigInteger bnj = new BigInteger(String.valueOf(j));
			final BigInteger evaluate = gp.evaluate(bnj);
			points[j - 1] = new Point(bnj, evaluate);
		}

		return new LagrangePolynomial(Arrays.asList(points), modulus);
	}

	/**
	 * Perform basic health checks on the share. Checks if the modulus is prime, if the threshold and number of parts are positive, and that the
	 * threshold smaller or equal than the number of parts
	 *
	 * <p>throws an {@link IllegalArgumentException} if shares have illegal values.
	 */
	private static void checkHealth(final ShamirShare shamirShare) {
		if (!shamirShare.getModulus().isProbablePrime(getCertaintyForLength(shamirShare.getModulus().bitLength()))) {
			throw new IllegalArgumentException("Modulus is not prime");
		}

		if (shamirShare.getNumberOfSecrets() <= 0) {
			throw new IllegalArgumentException("There must be a positive value for number of secrets");
		}

		if (shamirShare.getThreshold() <= 0) {
			throw new IllegalArgumentException("There must be a positive value for threshold");
		}

		if (shamirShare.getNumberOfParts() <= 0) {
			throw new IllegalArgumentException("There must be a positive value for number of parts");
		}

		if (shamirShare.getThreshold() > shamirShare.getNumberOfParts()) {
			throw new IllegalArgumentException("Threshold must be less than or equal to the number of parts");
		}

		if (shamirShare.getSecretLength() <= 0) {
			throw new IllegalArgumentException("The secret lenght must be a positive value.");
		}
	}

	/**
	 * Check if the given {@link Share} are compatible. throws an {@link IllegalArgumentException} if shares are not compatible.
	 *
	 * @param share one of the shares to check compatibility with.
	 * @param other the share to check compatibility with.
	 */
	private static void checkCompatible(final ShamirShare share, final ShamirShare other) {

		if (!share.getModulus().equals(other.getModulus())) {
			throw new IllegalArgumentException("Shares are not coherent, modulus are values different");
		}

		if (share.getThreshold() != other.getThreshold()) {
			throw new IllegalArgumentException("Shares are not coherent, threshold are values different");
		}

		if (share.getNumberOfParts() != other.getNumberOfParts()) {
			throw new IllegalArgumentException("Shares are not coherent, number of parts values are different");
		}

		if (share.getNumberOfSecrets() != other.getNumberOfSecrets()) {
			throw new IllegalArgumentException("Shares are not coherent, number of secrets values are different");
		}

		if (share.getSecretLength() != other.getSecretLength()) {
			throw new IllegalArgumentException("Shares are not coherent, size of secrets must be the same.");
		}
	}

	private static Set<ShamirShare> fromSetToShamirShares(final Set<Share> shares) {
		return shares.stream().map(ShamirShare.class::cast).collect(Collectors.toSet());
	}

	/**
	 * Convert a positive BigInteger to a fixed sized byte array.
	 *
	 * @param bi             a positive BigInteger
	 * @param expectedLength the length of the output byte array, in bytes.
	 * @return a byte array of the binary representation of the magnitude of the input BigInteger, padded to length.
	 * @throws IllegalArgumentException if the BigInteger is not positive or if the value cannot be represented in this number of bytes.
	 */
	private static byte[] toFixedSizeByteArray(final BigInteger bi, final int expectedLength) {
		if (bi.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("Undefined conversion from negative BigInteger to a fixed sized byte array.");
		}
		if (bi.bitLength() > expectedLength * Byte.SIZE) {
			throw new IllegalArgumentException(String.format("Cannot fully represent this BigInteger with %d bytes.", expectedLength));
		}

		// bi.toByteArray() outputs an array of length ceil((this.bitLength() +1)/8)) (from the BigInteger documentation)
		// Because of the previous check we are guaranteed that this array will be at most one byte longer than the expectedLength,
		// with the extra byte containing the sign bit. Since bi is positive this sign bit will be 0. Hence the output of
		// to bi.toByteArray() returns the magnitude in binary form padded with a single 0 byte.
		final byte[] magnitude = bi.toByteArray();
		final int length = magnitude.length;

		// If the length is the same we do nothing, as it already is padded with a 0
		if (length == expectedLength) {
			return magnitude;
		}
		// If the length is smaller we pad with 0s
		if (length < expectedLength) {
			final byte[] paddedMagnitude = new byte[expectedLength];
			final int paddingLength = expectedLength - length;
			System.arraycopy(magnitude, 0, paddedMagnitude, paddingLength, length);
			return paddedMagnitude;
		}
		// If the length is one more (the sign bit in a byte) we truncate it
		return Arrays.copyOfRange(magnitude, 1, length);
	}

	private static BigInteger toBigInteger(final byte[] secretBytes, final BigInteger modulus) {
		final BigInteger secretNumber = new BigInteger(1, secretBytes);
		if (secretNumber.compareTo(modulus) >= 0) {
			throw new IllegalArgumentException("Secret is too large");
		}

		return secretNumber;
	}

	@Override
	public Set<Share> split(final byte[][] secrets, final int number, final int threshold, final BigInteger modulus) {
		if (number < threshold) {
			throw new IllegalArgumentException("Threshold is greater than the number of shares to be generated");
		}

		// Check that all secrets have the same length.
		final int secretLength = secrets[0].length;
		Stream.of(secrets).filter(secret -> secret.length != secretLength).findAny().ifPresent(s -> {
			throw new IllegalArgumentException("Cannot create shares for secrets of different size.");
		});

		final LagrangePolynomial[] toSave = Stream.of(secrets)
				// Converts the byte array with the secrets to a number. A signum 1 is used as the Shamir secret sharing scheme is
				// defined for secrets between 0 (inclusive) and modulus (exclusive)
				// Checks also that the number does not exceed the modulus.
				.map(secretBytes -> toBigInteger(secretBytes, modulus))
				// Split the secrets.
				.map(secretNumber -> splitSecret(secretNumber, number, threshold, modulus))
				// Collect all split secrets into an array.
				.toArray(LagrangePolynomial[]::new);

		final Set<Share> shares = new HashSet<>();

		// generate the share values using consecutive values
		for (int j = 1; j <= number; ++j) {
			final int i = j - 1;
			shares.add(new ShamirShare(number, threshold, modulus, secretLength,
					Arrays.stream(toSave).map(LagrangePolynomial::getPoints).map(l -> l.get(i)).collect(Collectors.toList())));
		}

		Stream.of(secrets).forEach(s -> Arrays.fill(s, (byte) 0));

		return shares;
	}

	@Override
	public byte[][] recover(final Set<Share> shares, final int expectedSecrets) {
		if (shares == null || shares.isEmpty()) {
			throw new IllegalArgumentException("Empty share list");
		}

		final Set<ShamirShare> shamirShares = fromSetToShamirShares(shares);
		final ShamirShare first = shamirShares.iterator().next();
		checkHealth(first);
		if (first.getNumberOfSecrets() != expectedSecrets) {
			throw new IllegalArgumentException("This set of shares encodes " + first.getNumberOfSecrets() + " secrets. Expected " + expectedSecrets);
		}
		final byte[][] allSecrets = new byte[expectedSecrets][];
		for (final ShamirShare share : shamirShares) {
			checkCompatible(share, first);
		}

		for (int nthSecret = 0; nthSecret < expectedSecrets; nthSecret++) {
			final Set<Point> points = new HashSet<>();
			final BigInteger modulus = shamirShares.iterator().next().getModulus();
			for (final ShamirShare share : shamirShares) {
				points.add(share.getPoints().get(nthSecret));
			}
			// Using set so that repeated points do not count twice.
			if (points.size() < first.getThreshold()) {
				throw new IllegalArgumentException("Underflow: given number of shares is lower than threshold");
			}

			final BigInteger secretBigInteger;
			// If the threshold is one and there is only one point, it means all points of all shares have the same y-coordinate.
			// Furthermore, the Lagrange interpolation is not needed as the polynomial is of degree 0 (just a constant).
			if (first.getThreshold() == 1 && points.size() == 1) {
				secretBigInteger = first.getPoints().get(nthSecret).getY();
			} else {
				secretBigInteger = new LagrangePolynomial(new ArrayList<>(points), modulus).evaluateAtZero();
			}

			// Check if recovered secret needs padding or truncation.
			final int secretLength = first.getSecretLength();
			final byte[] secret = toFixedSizeByteArray(secretBigInteger, secretLength);

			allSecrets[nthSecret] = secret;
		}

		return allSecrets;
	}

	/**
	 * Gets the certainty level corresponding to the bit length of the number, according to FIPS-186-4 Table C.1.
	 *
	 * @param length length in bits.
	 * @return a certainty level.
	 */
	private static int getCertaintyForLength(final int length) {
		if (length <= 1024) {
			return 80;
		} else if (length <= 2048) {
			return 112;
		} else {
			return 128;
		}
	}
}
