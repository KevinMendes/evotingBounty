/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigSigningKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

/**
 * Tests of the asymmetric service API.
 */
class AsymmetricServiceTest {

	private static final int MAXIMUM_DATA_ARRAY_LENGTH = 10;

	private static PolicyFromPropertiesHelper defaultPolicy;

	private static AsymmetricService asymmetricServiceForDefaultPolicy;

	private static PublicKey publicKeyForEncryption;

	private static PrivateKey privateKeyForDecryption;

	private static PublicKey publicKeyForVerification;

	private static PrivateKey privateKeyForSigning;

	private static int dataByteLength;

	private static byte[] data;

	private static KeyPairConverterAPI keyPairConverter;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		final Properties properties = new Properties();
		properties.setProperty("asymmetric.signingkeypair", "RSA_2048_F4_SUN_RSA_SIGN");
		properties.setProperty("asymmetric.encryptionkeypair", "RSA_2048_F4_SUN_RSA_SIGN");
		defaultPolicy = new PolicyFromPropertiesHelper(properties);

		asymmetricServiceForDefaultPolicy = new AsymmetricService();

		final KeyPair keyPairForEncryption = asymmetricServiceForDefaultPolicy.getKeyPairForEncryption();
		publicKeyForEncryption = keyPairForEncryption.getPublic();
		privateKeyForDecryption = keyPairForEncryption.getPrivate();

		final KeyPair keyPairForSigning = asymmetricServiceForDefaultPolicy.getKeyPairForSigning();
		publicKeyForVerification = keyPairForSigning.getPublic();
		privateKeyForSigning = keyPairForSigning.getPrivate();

		dataByteLength = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH);

		data = PrimitivesTestDataGenerator.getByteArray(dataByteLength);

		keyPairConverter = asymmetricServiceForDefaultPolicy.getKeyPairConverter();
	}

	@Test
	void testWhenCreateEncryptionCryptoKeyPairThenExpectedAlgorithm() {

		final String publicKeyAlgorithm = publicKeyForEncryption.getAlgorithm();
		final ConfigSigningKeyPairAlgorithmAndSpec encryptionKeyPairAlgorithmAndSpec = ConfigSigningKeyPairAlgorithmAndSpec
				.valueOf(defaultPolicy.getPropertyValue("asymmetric.encryptionkeypair"));

		Assertions.assertEquals(encryptionKeyPairAlgorithmAndSpec.getAlgorithm(), publicKeyAlgorithm, "The algorithm was not the expected one.");
	}

	@Test
	void testWhenCreateSigningCryptoKeyPairThenExpectedAlgorithm() {

		final String publicKeyAlgorithm = publicKeyForVerification.getAlgorithm();
		final ConfigSigningKeyPairAlgorithmAndSpec signingKeyPairAlgorithmAndSpec = ConfigSigningKeyPairAlgorithmAndSpec
				.valueOf(defaultPolicy.getPropertyValue("asymmetric.signingkeypair"));

		Assertions.assertEquals(signingKeyPairAlgorithmAndSpec.getAlgorithm(), publicKeyAlgorithm, "The algorithm was not the expected one.");
	}

	@Test
	void testWhenAsymmetricallyEncryptAndDecryptThenOk() throws GeneralCryptoLibException {

		final byte[] encryptedData = asymmetricServiceForDefaultPolicy.encrypt(publicKeyForEncryption, data);

		final byte[] decryptedData = asymmetricServiceForDefaultPolicy.decrypt(privateKeyForDecryption, encryptedData);

		Assertions.assertArrayEquals(decryptedData, data);
	}

	@Test
	void testWhenSignAndVerifySignatureThenOk() throws GeneralCryptoLibException {

		final byte[] signature = asymmetricServiceForDefaultPolicy.sign(privateKeyForSigning, data);

		final boolean verified = asymmetricServiceForDefaultPolicy.verifySignature(signature, publicKeyForVerification, data);

		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenSignAndVerifySignatureForMultipleDataElementsThenOk() throws GeneralCryptoLibException {

		final byte[][] dataArray = PrimitivesTestDataGenerator
				.getByteArrayArray(dataByteLength, CommonTestDataGenerator.getInt(2, MAXIMUM_DATA_ARRAY_LENGTH));

		final byte[] signature = asymmetricServiceForDefaultPolicy.sign(privateKeyForSigning, dataArray);

		final boolean verified = asymmetricServiceForDefaultPolicy.verifySignature(signature, publicKeyForVerification, dataArray);

		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenSignAndVerifySignatureForDataInputStreamThenOk() throws GeneralCryptoLibException, IOException {

		final boolean verified;
		try (final InputStream dataInputStream = new ByteArrayInputStream(data)) {

			final byte[] signature = asymmetricServiceForDefaultPolicy.sign(privateKeyForSigning, dataInputStream);

			dataInputStream.reset();

			verified = asymmetricServiceForDefaultPolicy.verifySignature(signature, publicKeyForVerification, dataInputStream);
		}

		Assertions.assertTrue(verified);
	}

	@Test
	void testWhenConvertKeyPairForEncryptionToAndFromPemThenOk() throws GeneralCryptoLibException {

		final String publicKeyPem = keyPairConverter.exportPublicKeyForEncryptingToPem(publicKeyForEncryption);
		final PublicKey publicKey = keyPairConverter.getPublicKeyForEncryptingFromPem(publicKeyPem);
		Assertions.assertArrayEquals(publicKey.getEncoded(), publicKeyForEncryption.getEncoded());

		final String privateKeyPem = keyPairConverter.exportPrivateKeyForEncryptingToPem(privateKeyForDecryption);
		final PrivateKey privateKey = keyPairConverter.getPrivateKeyForEncryptingFromPem(privateKeyPem);
		Assertions.assertArrayEquals(privateKey.getEncoded(), privateKeyForDecryption.getEncoded());
	}

	@Test
	void testWhenConvertKeyPairForSigningToAndFromPemThenOk() throws GeneralCryptoLibException {

		final String publicKeyPem = keyPairConverter.exportPublicKeyForEncryptingToPem(publicKeyForVerification);
		final PublicKey publicKey = keyPairConverter.getPublicKeyForEncryptingFromPem(publicKeyPem);
		Assertions.assertArrayEquals(publicKey.getEncoded(), publicKeyForVerification.getEncoded());

		final String privateKeyPem = keyPairConverter.exportPrivateKeyForEncryptingToPem(privateKeyForSigning);
		final PrivateKey privateKey = keyPairConverter.getPrivateKeyForEncryptingFromPem(privateKeyPem);
		Assertions.assertArrayEquals(privateKey.getEncoded(), privateKeyForSigning.getEncoded());
	}
}
