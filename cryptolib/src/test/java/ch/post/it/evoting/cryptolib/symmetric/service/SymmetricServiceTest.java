/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.primes.bean.TestCryptoDerivedKey;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

/**
 * Tests of the symmetric service API.
 */
class SymmetricServiceTest {

	private static final int MAXIMUM_DATA_ARRAY_LENGTH = 10;

	private static final int LARGE_DATA_LENGTH = 10000000;

	private static SymmetricService symmetricServiceForDefaultPolicy;

	private static SecretKey secretKeyForEncryption;

	private static SecretKey secretKeyForMac;

	private static int dataByteLength;

	private static byte[] data;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		symmetricServiceForDefaultPolicy = new SymmetricService();

		secretKeyForEncryption = symmetricServiceForDefaultPolicy.getSecretKeyForEncryption();

		secretKeyForMac = symmetricServiceForDefaultPolicy.getSecretKeyForHmac();

		dataByteLength = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH);

		data = PrimitivesTestDataGenerator.getByteArray(dataByteLength);
	}

	@Test
	void testWhenSymmetricallyEncryptAndDecryptThenOk() throws GeneralCryptoLibException {

		final byte[] encryptedData = symmetricServiceForDefaultPolicy.encrypt(secretKeyForEncryption, data);

		final byte[] decryptedData = symmetricServiceForDefaultPolicy.decrypt(secretKeyForEncryption, encryptedData);

		Assertions.assertArrayEquals(decryptedData, data);
	}

	@Test
	void testWhenSymmetricallyEncryptAndDecryptWithLargeDataThenOk() throws GeneralCryptoLibException {

		final byte[] data = PrimitivesTestDataGenerator.getByteArray(LARGE_DATA_LENGTH);

		final byte[] encryptedData = symmetricServiceForDefaultPolicy.encrypt(secretKeyForEncryption, data);

		final byte[] decryptedData = symmetricServiceForDefaultPolicy.decrypt(secretKeyForEncryption, encryptedData);

		Assertions.assertArrayEquals(decryptedData, data);
	}

	@Test
	void testWhenGenerateAndVerifyMacThenOk() throws GeneralCryptoLibException {

		final byte[] mac = symmetricServiceForDefaultPolicy.getMac(secretKeyForMac, data);

		final boolean verified = symmetricServiceForDefaultPolicy.verifyMac(secretKeyForMac, mac, data);

		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenGenerateAndVerifyMacForMultipleDataElementsThenOk() throws GeneralCryptoLibException {

		final byte[][] dataArray = PrimitivesTestDataGenerator
				.getByteArrayArray(dataByteLength, CommonTestDataGenerator.getInt(2, MAXIMUM_DATA_ARRAY_LENGTH));

		final byte[] mac = symmetricServiceForDefaultPolicy.getMac(secretKeyForMac, dataArray);

		final boolean verified = symmetricServiceForDefaultPolicy.verifyMac(secretKeyForMac, mac, dataArray);

		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenGenerateAndVerifyMacFromDataInputStreamThenOk() throws GeneralCryptoLibException, IOException {

		final boolean verified;
		try (final InputStream dataInputStream = new ByteArrayInputStream(data)) {
			final byte[] mac = symmetricServiceForDefaultPolicy.getMac(secretKeyForMac, dataInputStream);
			dataInputStream.reset();
			verified = symmetricServiceForDefaultPolicy.verifyMac(secretKeyForMac, mac, dataInputStream);
		}
		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenGetSecretKeyForEncryptionFromDerivedKeyThenOk() throws GeneralCryptoLibException {

		final TestCryptoDerivedKey derivedKeyForEncryption = new TestCryptoDerivedKey(secretKeyForEncryption.getEncoded());

		final SecretKey secretKey = symmetricServiceForDefaultPolicy.getSecretKeyForEncryptionFromDerivedKey(derivedKeyForEncryption);

		Assertions.assertArrayEquals(secretKey.getEncoded(), secretKeyForEncryption.getEncoded());
	}

	@Test
	void testWhenGetSecretKeyForMacFromDerivedKeyThenOk() throws GeneralCryptoLibException {

		final TestCryptoDerivedKey derivedKeyForMac = new TestCryptoDerivedKey(secretKeyForMac.getEncoded());

		final SecretKey secretKey = symmetricServiceForDefaultPolicy.getSecretKeyForMacFromDerivedKey(derivedKeyForMac);

		Assertions.assertArrayEquals(secretKey.getEncoded(), secretKeyForMac.getEncoded());
	}

	@Test
	void testWhenGetSecretKeyForEncryptionFromBytesThenOk() throws GeneralCryptoLibException {

		final SecretKey secretKey = symmetricServiceForDefaultPolicy.getSecretKeyForEncryptionFromBytes(secretKeyForEncryption.getEncoded());

		Assertions.assertArrayEquals(secretKey.getEncoded(), secretKeyForEncryption.getEncoded());
	}
}
