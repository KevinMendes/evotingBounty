/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing.shamir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.cryptolib.secretsharing.shamir.ShamirShare;

class ShamirSecretSharingSchemeTest {

	private ThresholdSecretSharingService secretSharingService;

	@BeforeEach
	void initialize() {
		this.secretSharingService = new ThresholdSecretSharingService();
	}

	@Test
	void testSecretWithFirstBits0() {
		final byte[] secret = new byte[] { (byte) 0x00, (byte) 0x01 };
		final byte[] secretClone = secret.clone();
		final BigInteger modulus = new BigInteger(1, secret).nextProbablePrime();

		final Set<Share> split = secretSharingService.split(secretClone, 2, 1, modulus);
		final byte[] recoveredSecret = secretSharingService.recover(split);

		// Make sure the secret is destroyed
		assertArrayEquals(new byte[secret.length], secretClone);

		// Check the recovered secret
		assertArrayEquals(secret, recoveredSecret);
	}

	@Test
	void testGoThereAndBackAgainWithSecretLeadingBit1() {
		// We generate a 2048 bit secret
		final byte[] secret = new byte[256];
		Arrays.fill(secret, (byte) -128);

		final byte[] clonedSecret = secret.clone();

		final Set<Share> shares = secretSharingService.split(clonedSecret, 2, 1, new BigInteger(1, secret).nextProbablePrime());

		// Make sure the secret has been disposed
		assertArrayEquals(new byte[clonedSecret.length], clonedSecret);

		assertArrayEquals(secret, secretSharingService.recover(shares));
		final Share[] shareArray = shares.toArray(new Share[0]);

		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 0, 1)))));
		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 1, 2)))));
	}

	@Test
	void testGoThereAndBackAgainWithThresholdOne() {
		// We generate a 2048 bit secret
		final byte[] secret = new byte[256];
		Arrays.fill(secret, (byte) 1);

		final byte[] clonedSecret = secret.clone();

		final Set<Share> shares = secretSharingService.split(clonedSecret, 2, 1, new BigInteger(1, secret).nextProbablePrime());

		// Make sure the secret has been disposed
		assertArrayEquals(new byte[clonedSecret.length], clonedSecret);

		assertArrayEquals(secret, secretSharingService.recover(shares));
		final Share[] shareArray = shares.toArray(new Share[0]);

		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 0, 1)))));
		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 1, 2)))));
	}

	@Test
	void testGoThereAndBackAgain() {
		// We generate a 2048 bit secret
		final byte[] secret = new byte[256];
		Arrays.fill(secret, (byte) 1);

		final byte[] clonedSecret = secret.clone();

		final Set<Share> shares = secretSharingService.split(clonedSecret, 5, 3, new BigInteger(1, secret).nextProbablePrime());

		// Make sure the secret has been disposed
		assertArrayEquals(new byte[clonedSecret.length], clonedSecret);

		assertArrayEquals(secret, secretSharingService.recover(shares));
		final Share[] shareArray = shares.toArray(new Share[0]);

		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 0, 3)))));
		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 1, 4)))));
		assertArrayEquals(secret, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 2, 5)))));
	}

	@Test
	void testMultipleGoThereAndBackAgain() {
		final byte[][] secrets = new byte[][] { { 1, 1, 1 }, { 2, 2, 2 }, { 3, 3, 3 }, { 4, 4, 4 }, { 5, 5, 5 }, { 6, 6, 6 } };
		final int numSecrets = secrets.length;

		// Make the modulus larger than the largest secret.
		final BigInteger modulus = new BigInteger(1, secrets[secrets.length - 1]).nextProbablePrime();

		// Clone the secrets since they are going to be removed.
		final byte[][] clonedSecrets = Stream.of(secrets).map(byte[]::clone).toArray(byte[][]::new);

		final Set<Share> shares = secretSharingService.split(clonedSecrets, 5, 3, modulus);

		// Make sure the secrets has been disposed
		assertArrayEquals(new byte[clonedSecrets.length][clonedSecrets[0].length], clonedSecrets);

		// Test recover with all shares
		final byte[][] recoveredSecrets = secretSharingService.recover(shares, numSecrets);
		assertArrayEquals(secrets, recoveredSecrets);

		// Test recover with subshares
		final Share[] shareArray = shares.toArray(new Share[0]);

		assertArrayEquals(secrets, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 0, 3))), numSecrets));
		assertArrayEquals(secrets, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 1, 4))), numSecrets));
		assertArrayEquals(secrets, secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 2, 5))), numSecrets));
	}

	@Test
	void testNegativeSecret() {
		final byte[] secret = new byte[] { 0, -5 };
		final byte[] secretClone = secret.clone();
		final BigInteger modulus = new BigInteger(1, secret).nextProbablePrime();

		final Set<Share> split = secretSharingService.split(secretClone, 2, 1, modulus);
		final byte[] recoveredSecret = secretSharingService.recover(split);

		// Make sure the secret is destroyed
		assertArrayEquals(new byte[secret.length], secretClone);

		// Check the recovered secret.
		assertArrayEquals(secret, recoveredSecret);
	}

	@Test
	void testZeroSecret() {
		final byte[] zeroSecret = new byte[2048];
		final BigInteger modulus = new BigInteger(new byte[] { 2 });

		final Set<Share> split = secretSharingService.split(zeroSecret, 2, 1, modulus);
		final byte[] recoveredSecret = secretSharingService.recover(split);

		assertEquals(new BigInteger(zeroSecret), new BigInteger(recoveredSecret));
	}

	@Test
	void notEnoughShares() {

		// We generate a 2048 bit secret
		final byte[] secret = new byte[256];
		Arrays.fill(secret, (byte) 1);

		final byte[] clonedSecret = secret.clone();

		final Set<Share> shares = secretSharingService.split(clonedSecret, 5, 3, new BigInteger(1, secret).nextProbablePrime());

		final Share[] shareArray = shares.toArray(new Share[0]);

		assertThrows(IllegalArgumentException.class,
				() -> secretSharingService.recover(new HashSet<>(Arrays.asList(Arrays.copyOfRange(shareArray, 0, 1)))));
	}

	@Test
	void oversizedSecret() {
		final byte[][] secrets = new byte[][] { { 45 }, { 46 }, { 47 }, { 48 }, { 49 }, { 52 } };
		final BigInteger modulus = new BigInteger(new byte[] { 51 });

		// The method should complain about the last element.
		assertThrows(IllegalArgumentException.class, () -> secretSharingService.split(secrets, 3, 2, modulus));
	}

	@Test
	void slightlyOversizedSecret() {
		final byte secretValue = 1;
		final byte[][] secrets = new byte[][] { { secretValue } };
		final BigInteger modulus = new BigInteger(new byte[] { secretValue });

		// The method should complain.
		assertThrows(IllegalArgumentException.class, () -> secretSharingService.split(secrets, 3, 2, modulus));
	}

	@Test
	void testDifferentLengthSecretsShouldThrow() {
		final byte[][] secrets = new byte[][] { { 1, 1, 1 }, { 2, 2, 2 }, { 3, 3 }, { 4, 4, 4 }, { 5, 5, 5 }, { 6, 6, 6 } };

		// Make the modulus larger than the largest secret.
		final BigInteger modulus = new BigInteger(1, secrets[secrets.length - 1]).nextProbablePrime();

		// Clone the secrets since they are going to be removed.
		final byte[][] clonedSecrets = Stream.of(secrets).map(byte[]::clone).toArray(byte[][]::new);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> secretSharingService.split(clonedSecrets, 3, 2, modulus));
		assertEquals("Cannot create shares for secrets of different size.", exception.getMessage());
	}

	@Test
	void testNegativeRecoveredSecretException() {
		final BigInteger secret = new BigInteger(new byte[] { (byte) -1 });
		final Point point = new Point(BigInteger.ONE, secret);
		final BigInteger modulus = new BigInteger(new byte[] { (byte) 1 });
		final Share share = new ShamirShare(2, 1, modulus, 1, Collections.singletonList(point));

		final Set<Share> shares = new HashSet<>();
		shares.add(share);

		assertThrows(IllegalArgumentException.class, () -> secretSharingService.recover(shares));
	}

	@Test
	void testTooBigRecoveredSecretException() {
		final BigInteger secret = new BigInteger(1, new byte[] { (byte) 255, (byte) 255 });
		final Point point = new Point(BigInteger.ONE, secret);
		final BigInteger modulus = secret.nextProbablePrime();
		final Share share = new ShamirShare(2, 1, modulus, 1, Collections.singletonList(point));

		final Set<Share> shares = new HashSet<>();
		shares.add(share);

		assertThrows(IllegalArgumentException.class, () -> secretSharingService.recover(shares));
	}

	@Test
	void testCheckKOPrime() {
		final Point point = new Point(BigInteger.ZERO, BigInteger.ZERO);
		final ShamirShare share = new ShamirShare(0, 0, new BigInteger(new byte[] { 0 }), 0, Collections.singletonList(point));
		final Set<Share> shares = new HashSet<>();
		shares.add(share);

		assertThrows(IllegalArgumentException.class, () -> secretSharingService.recover(shares));
	}

	@Test
	void testCheckKOThreshold() {
		final Point point = new Point(new BigInteger(new byte[] { 1 }), new BigInteger(new byte[] { 1 }));
		final ShamirShare share = new ShamirShare(0, 1, new BigInteger(new byte[] { 2 }), 0, Collections.singletonList(point));
		final Set<Share> shares = new HashSet<>();
		shares.add(share);

		assertThrows(IllegalArgumentException.class, () -> secretSharingService.recover(shares));
	}

}
