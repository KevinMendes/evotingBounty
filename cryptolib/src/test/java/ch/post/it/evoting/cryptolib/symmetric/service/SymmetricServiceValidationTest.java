/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.primes.bean.TestCryptoDerivedKey;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicyFromProperties;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestSecretKey;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class SymmetricServiceValidationTest {

	private static final int MAXIMUM_DATA_ARRAY_LENGTH = 10;

	private static SymmetricService symmetricServiceForDefaultPolicy;
	private static SecretKey secretKeyForEncryption;
	private static SecretKey secretKeyForMac;
	private static byte[] data;
	private static byte[][] dataArray;
	private static byte[] mac;
	private static byte[] emptyByteArray;
	private static byte[][] emptyByteArrayArray;
	private static TestSecretKey nullContentSecretKey;
	private static TestSecretKey emptyContentSecretKey;
	private static TestCryptoDerivedKey nullContentDerivedKey;
	private static TestCryptoDerivedKey emptyContentDerivedKey;
	private static byte[][] dataArrayConsistingOfNullElement;
	private static byte[][] dataArrayConsistingOfEmptyElement;
	private static byte[][] dataArrayContainingNullElement;
	private static byte[][] dataArrayContainingEmptyElement;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		symmetricServiceForDefaultPolicy = new SymmetricService();

		secretKeyForEncryption = symmetricServiceForDefaultPolicy.getSecretKeyForEncryption();

		secretKeyForMac = symmetricServiceForDefaultPolicy.getSecretKeyForHmac();

		final int dataByteLength = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH);

		final int dataArrayLength = CommonTestDataGenerator.getInt(2, MAXIMUM_DATA_ARRAY_LENGTH);

		data = PrimitivesTestDataGenerator.getByteArray(dataByteLength);

		dataArray = PrimitivesTestDataGenerator.getByteArrayArray(dataByteLength, CommonTestDataGenerator.getInt(2, MAXIMUM_DATA_ARRAY_LENGTH));

		mac = symmetricServiceForDefaultPolicy.getMac(secretKeyForMac, data);

		emptyByteArray = new byte[0];
		emptyByteArrayArray = new byte[0][0];

		nullContentSecretKey = new TestSecretKey(null);
		emptyContentSecretKey = new TestSecretKey(emptyByteArray);

		nullContentDerivedKey = new TestCryptoDerivedKey(null);
		emptyContentDerivedKey = new TestCryptoDerivedKey(emptyByteArray);

		dataArrayConsistingOfNullElement = new byte[1][dataByteLength];
		dataArrayConsistingOfNullElement[0] = null;

		dataArrayConsistingOfEmptyElement = new byte[1][dataByteLength];
		dataArrayConsistingOfEmptyElement[0] = emptyByteArray;

		dataArrayContainingNullElement = PrimitivesTestDataGenerator.getByteArrayArray(dataByteLength, dataArrayLength);
		dataArrayContainingNullElement[0] = null;

		dataArrayContainingEmptyElement = PrimitivesTestDataGenerator.getByteArrayArray(dataByteLength, dataArrayLength);
		dataArrayContainingEmptyElement[0] = emptyByteArray;
	}

	static Stream<Arguments> symmetricallyEncrypt() {

		return Stream.of(arguments(null, data, "Secret key is null."), arguments(nullContentSecretKey, data, "Secret key content is null."),
				arguments(emptyContentSecretKey, data, "Secret key content is empty."), arguments(secretKeyForEncryption, null, "Data is null."),
				arguments(secretKeyForEncryption, emptyByteArray, "Data is empty."));
	}

	static Stream<Arguments> symmetricallyDecrypt() throws GeneralCryptoLibException {

		final byte[] encryptedData = symmetricServiceForDefaultPolicy.encrypt(secretKeyForEncryption, data);

		return Stream.of(arguments(null, encryptedData, "Secret key is null."),
				arguments(nullContentSecretKey, encryptedData, "Secret key content is null."),
				arguments(emptyContentSecretKey, encryptedData, "Secret key content is empty."),
				arguments(secretKeyForEncryption, null, "Encrypted data is null."),
				arguments(secretKeyForEncryption, emptyByteArray, "Encrypted data is empty."));
	}

	static Stream<Arguments> generateMac() {

		return Stream.of(arguments(null, dataArray, "Secret key is null."), arguments(nullContentSecretKey, dataArray, "Secret key content is null."),
				arguments(emptyContentSecretKey, dataArray, "Secret key content is empty."),
				arguments(secretKeyForMac, null, "Data element array is null."),
				arguments(secretKeyForMac, emptyByteArrayArray, "Data element array is empty."),
				arguments(secretKeyForMac, dataArrayConsistingOfNullElement, "Data is null."),
				arguments(secretKeyForMac, dataArrayConsistingOfEmptyElement, "Data is empty."),
				arguments(secretKeyForMac, dataArrayContainingNullElement, "A data element is null."),
				arguments(secretKeyForMac, dataArrayContainingEmptyElement, "A data element is empty."));
	}

	static Stream<Arguments> verifyMac() {

		return Stream.of(arguments(null, mac, dataArray, "Secret key is null."),
				arguments(nullContentSecretKey, mac, dataArray, "Secret key content is null."),
				arguments(emptyContentSecretKey, mac, dataArray, "Secret key content is empty."),
				arguments(secretKeyForMac, null, dataArray, "MAC is null."), arguments(secretKeyForMac, emptyByteArray, dataArray, "MAC is empty."),
				arguments(secretKeyForMac, mac, null, "Data element array is null."),
				arguments(secretKeyForMac, mac, emptyByteArrayArray, "Data element array is empty."),
				arguments(secretKeyForMac, mac, dataArrayConsistingOfNullElement, "Data is null."),
				arguments(secretKeyForMac, mac, dataArrayConsistingOfEmptyElement, "Data is empty."),
				arguments(secretKeyForMac, mac, dataArrayContainingNullElement, "A data element is null."),
				arguments(secretKeyForMac, mac, dataArrayContainingEmptyElement, "A data element is empty."));
	}

	static Stream<Arguments> generateMacFromDataInputStream() {

		final InputStream dataInputStream = new ByteArrayInputStream(data);

		return Stream.of(arguments(null, dataInputStream, "Secret key is null."),
				arguments(nullContentSecretKey, dataInputStream, "Secret key content is null."),
				arguments(emptyContentSecretKey, dataInputStream, "Secret key content is empty."),
				arguments(secretKeyForMac, null, "Data input stream is null."));
	}

	static Stream<Arguments> verifyMacFromDataInputStream() {

		final InputStream dataInputStream = new ByteArrayInputStream(data);

		return Stream.of(arguments(null, mac, dataInputStream, "Secret key is null."),
				arguments(nullContentSecretKey, mac, dataInputStream, "Secret key content is null."),
				arguments(emptyContentSecretKey, mac, dataInputStream, "Secret key content is empty."),
				arguments(secretKeyForMac, null, dataInputStream, "MAC is null."),
				arguments(secretKeyForMac, emptyByteArray, dataInputStream, "MAC is empty."),
				arguments(secretKeyForMac, mac, null, "Data input stream is null."));
	}

	static Stream<Arguments> getSecretKeyForEncryptionFromDerivedKey() {

		return Stream.of(arguments(null, "Derived key is null."), arguments(nullContentDerivedKey, "Derived key content is null."),
				arguments(emptyContentDerivedKey, "Derived key content is empty."));
	}

	static Stream<Arguments> getSecretKeyForMacFromDerivedKey() {

		return Stream.of(arguments(null, "Derived key is null."), arguments(nullContentDerivedKey, "Derived key content is null."),
				arguments(emptyContentDerivedKey, "Derived key content is empty."));
	}

	static Stream<Arguments> getSecretKeyForEncryptionFromBytes() {

		return Stream.of(arguments(null, "Secret key is null."), arguments(emptyByteArray, "Secret key is empty."));
	}

	@ParameterizedTest
	@MethodSource("symmetricallyEncrypt")
	void testSymmetricEncryptionValidation(final SecretKey key, final byte[] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.encrypt(key, data));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("symmetricallyDecrypt")
	void testSymmetricDecryptionValidation(final SecretKey key, final byte[] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.decrypt(key, data));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateMac")
	void testMacGenerationValidation(final SecretKey key, final byte[][] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.getMac(key, data));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("verifyMac")
	void testMacVerificationFromDataInputStreamValidation(final SecretKey key, final byte[] mac, final byte[][] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.verifyMac(key, mac, data));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateMacFromDataInputStream")
	void testMacGenerationFromDataInputStreamValidation(final SecretKey key, final InputStream inStream, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.getMac(key, inStream));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("verifyMacFromDataInputStream")
	void testMacVerificationFromDataInputStreamValidation(final SecretKey key, final byte[] mac, final InputStream inStream, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.verifyMac(key, mac, inStream));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getSecretKeyForEncryptionFromDerivedKey")
	void testSecretKeyForEncryptionFromDerivedKeyValidation(final CryptoAPIDerivedKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.getSecretKeyForEncryptionFromDerivedKey(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getSecretKeyForMacFromDerivedKey")
	void testSecretKeyForMacFromDerivedKeyValidation(final CryptoAPIDerivedKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.getSecretKeyForMacFromDerivedKey(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getSecretKeyForEncryptionFromBytes")
	void testSecretKeyForEncryptionFromBytesValidation(final byte[] keyBytes, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.getSecretKeyForEncryptionFromBytes(keyBytes));
		assertEquals(errorMsg, exception.getMessage());
	}

	@Test
	void encryptWithUnsupportedKeyLengthTest() {
		final SymmetricKeyPolicy symmetricKeyPolicy = new SymmetricKeyPolicyFromProperties();
		final int bitsLength = symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec().getKeyLength();
		final byte[] key = new byte[bitsLength / 4];
		final SecretKey secretKeyCreated = new SecretKeySpec(key, "AES");

		final String errorMsg =
				"The specified key's length must be equal to symmetric key length in the policy: " + bitsLength + "; Found " + key.length * 8;
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> symmetricServiceForDefaultPolicy.encrypt(secretKeyCreated, "testMessage".getBytes(StandardCharsets.UTF_8)));
		assertEquals(errorMsg, exception.getMessage());
	}
}
