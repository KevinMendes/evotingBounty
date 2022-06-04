/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing.shamir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingServiceAPI;
import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.cryptolib.secretsharing.shamir.ShamirShare;

class ShamirShareSerializerTest {

	private static ThresholdSecretSharingServiceAPI secretSharingService;

	@BeforeAll
	static void setUp() {
		secretSharingService = new ThresholdSecretSharingService();
	}

	@Test
	void testBuild() throws GeneralCryptoLibException {
		final int numberOfParts = 10;
		final int threshold = 5;
		final SecureRandom secureRandom = new SecureRandom();
		final BigInteger modulus = BigInteger.probablePrime(500, secureRandom);
		final Point point = new Point(BigInteger.probablePrime(500, secureRandom), BigInteger.probablePrime(500, secureRandom));
		final ShamirShare s = new ShamirShare(numberOfParts, threshold, modulus, 0, Collections.singletonList(point));
		final ShamirShare s2 = (ShamirShare) secretSharingService.deserialize(secretSharingService.serialize(s));

		assertEquals(s.getNumberOfParts(), s2.getNumberOfParts());
		assertEquals(s.getThreshold(), s2.getThreshold());
		assertEquals(s.getModulus(), s2.getModulus());
		assertEquals(s.getPoints().get(0).getX(), s2.getPoints().get(0).getX());
		assertEquals(s.getPoints().get(0).getY(), s2.getPoints().get(0).getY());
		assertEquals(s.getSecretLength(), s2.getSecretLength());
	}

	@Test
	void testOverflow() {
		final byte[] test = ByteBuffer.allocate(200).putInt(1).putInt(1).putShort((short) 1).put(new BigInteger("1").byteValue()).putShort((short) 1)
				.put(new BigInteger("1").byteValue()).putShort((short) 1).put(new BigInteger("1").byteValue()).array();

		assertThrows(GeneralCryptoLibException.class, () -> secretSharingService.deserialize(test));
	}

	@Test
	void testUnderflow() {
		final byte[] test = ByteBuffer.allocate(21).putInt(1).putInt(1).putInt(1).putShort(Short.MAX_VALUE).put(new BigInteger("1").byteValue())
				.putShort(Short.MAX_VALUE).put(new BigInteger("1").byteValue()).putShort(Short.MAX_VALUE).put(new BigInteger("1").byteValue())
				.array();

		assertThrows(GeneralCryptoLibException.class, () -> secretSharingService.deserialize(test));
	}
}
