/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.ConfigMessageDigestAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicy;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicyFromProperties;

class MessageDigestGenerationTest {

	private static CryptoMessageDigest cryptoMessageDigestFromProperties;

	private static CryptoMessageDigest cryptoMessageDigestSHA512_224;

	private final String TEST_DATA_1 = "test data 1";

	@BeforeAll
	public static void setUp() {

		cryptoMessageDigestFromProperties = new MessageDigestFactory(new MessageDigestPolicyFromProperties()).create();

		cryptoMessageDigestSHA512_224 = new MessageDigestFactory(getMessageDigestPolicySha512_224AndDefault()).create();
	}

	private static MessageDigestPolicy getMessageDigestPolicySha512_224AndDefault() {
		return () -> ConfigMessageDigestAlgorithmAndProvider.SHA512_224_DEFAULT;
	}

	@Test
	void whenGenerateHashThenHasExpectedLength() {

		byte[] messageDigest = cryptoMessageDigestFromProperties.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		int messageDigestByteLength = messageDigest.length;

		final int MESSAGE_DIGEST_SHA256_BYTE_LENGTH = 32;
		Assertions.assertEquals(messageDigestByteLength, MESSAGE_DIGEST_SHA256_BYTE_LENGTH);

		messageDigest = cryptoMessageDigestSHA512_224.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		messageDigestByteLength = messageDigest.length;

		final int MESSAGE_DIGEST_SHA512_224_BYTE_LENGTH = 28;
		Assertions.assertEquals(messageDigestByteLength, MESSAGE_DIGEST_SHA512_224_BYTE_LENGTH);
	}

	@Test
	void whenGenerateHashTwiceThenHashsAreEqual() {

		byte[] messageDigest1 = cryptoMessageDigestFromProperties.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		byte[] messageDigest2 = cryptoMessageDigestFromProperties.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		Assertions.assertArrayEquals(messageDigest1, messageDigest2);

		messageDigest1 = cryptoMessageDigestSHA512_224.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		messageDigest2 = cryptoMessageDigestSHA512_224.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		Assertions.assertArrayEquals(messageDigest1, messageDigest2);
	}

	@Test
	void whenGenerateHashsForDifferentDataThenDifferentHashs() {

		byte[] messageDigest1 = cryptoMessageDigestFromProperties.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		final String TEST_DATA_2 = "test data 2";
		byte[] messageDigest2 = cryptoMessageDigestFromProperties.generate(TEST_DATA_2.getBytes(StandardCharsets.UTF_8));

		Assertions.assertFalse(Arrays.equals(messageDigest1, messageDigest2));

		messageDigest1 = cryptoMessageDigestSHA512_224.generate(TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		messageDigest2 = cryptoMessageDigestSHA512_224.generate(TEST_DATA_2.getBytes(StandardCharsets.UTF_8));

		Assertions.assertFalse(Arrays.equals(messageDigest1, messageDigest2));
	}

	@Test
	void testHashFromStreamSmallFile() throws GeneralCryptoLibException, IOException {

		final byte[] hash;
		try (final InputStream in = constructStreamContainingBytes(1024)) {
			hash = cryptoMessageDigestFromProperties.generate(in);
		}

		final String hashBase64 = Base64.getEncoder().encodeToString(hash);
		final String expectedHashBase64 = "eFsHUfwsU9wUpM49gA5p75zhAJ6zJ8z0WK/gnCQsJsk=";

		Assertions.assertEquals(expectedHashBase64, hashBase64);
	}

	@Test
	void testHashFromStreamLargeFile() throws GeneralCryptoLibException, IOException {

		final byte[] hash;
		try (final InputStream inStream = constructStreamContainingBytes(5632)) {
			hash = cryptoMessageDigestFromProperties.generate(inStream);
		}

		final String hashBase64 = Base64.getEncoder().encodeToString(hash);

		final String expectedHashBase64 = "kcjIP7AE3JUkpb+yTfvb8hPGu92BFA+w1x7yL8DKGVY=";

		Assertions.assertEquals(expectedHashBase64, hashBase64);
	}

	@Test
	void confirmHashGeneratedUsingDifferentMethodsAreEqual() throws GeneralCryptoLibException, IOException {

		final byte[] bytes = constructBytes(5632);
		final byte[] hashFromStream;
		try (final InputStream stream = new ByteArrayInputStream(bytes)) {
			// get hash using stream
			hashFromStream = cryptoMessageDigestFromProperties.generate(stream);
		}

		// get hash by reading all of the bytes at once
		final byte[] hashFromBytes = cryptoMessageDigestFromProperties.generate(bytes);

		// confirm that both hashes are equal
		Assertions.assertArrayEquals(hashFromStream, hashFromBytes);
	}

	private byte[] constructBytes(final int count) {
		final byte[] bytes = new byte[count];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) i;
		}
		return bytes;
	}

	private InputStream constructStreamContainingBytes(final int numberBytes) {
		return new ByteArrayInputStream(constructBytes(numberBytes));
	}
}
