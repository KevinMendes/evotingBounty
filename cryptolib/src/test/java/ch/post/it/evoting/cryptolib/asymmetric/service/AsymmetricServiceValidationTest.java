/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPrivateKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class AsymmetricServiceValidationTest {

	private static final int MAXIMUM_DATA_ARRAY_LENGTH = 10;

	private static AsymmetricService asymmetricServiceForDefaultPolicy;
	private static String whiteSpaceString;
	private static PublicKey publicKeyForEncryption;
	private static PrivateKey privateKeyForDecryption;
	private static PublicKey publicKeyForVerification;
	private static PrivateKey privateKeyForSigning;
	private static byte[] data;
	private static byte[][] dataArray;
	private static InputStream dataInputStream;
	private static byte[] signature;
	private static KeyPairConverterAPI keyPairConverter;
	private static byte[] emptyByteArray;
	private static byte[][] emptyByteArrayArray;
	private static TestPublicKey nullContentPublicKey;
	private static TestPublicKey emptyContentPublicKey;
	private static TestPrivateKey nullContentPrivateKey;
	private static TestPrivateKey emptyContentPrivateKey;
	private static byte[][] dataArrayConsistingOfNullElement;
	private static byte[][] dataArrayConsistingOfEmptyElement;
	private static byte[][] dataArrayContainingNullElement;
	private static byte[][] dataArrayContainingEmptyElement;
	private static PublicKey illegalSizePublicKeyForEncryption;
	private static PrivateKey illegalSizePrivateKeyForDecryption;
	private static PublicKey illegalSizePublicKeyForVerification;
	private static PrivateKey illegalSizePrivateKeyForSigning;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		asymmetricServiceForDefaultPolicy = new AsymmetricService();

		whiteSpaceString = CommonTestDataGenerator
				.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH));

		KeyPair keyPair = asymmetricServiceForDefaultPolicy.getKeyPairForEncryption();
		publicKeyForEncryption = keyPair.getPublic();
		privateKeyForDecryption = keyPair.getPrivate();

		keyPair = asymmetricServiceForDefaultPolicy.getKeyPairForSigning();
		publicKeyForVerification = keyPair.getPublic();
		privateKeyForSigning = keyPair.getPrivate();

		final int dataByteLength = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH);

		data = PrimitivesTestDataGenerator.getByteArray(dataByteLength);

		dataArray = PrimitivesTestDataGenerator.getByteArrayArray(dataByteLength, CommonTestDataGenerator.getInt(2, MAXIMUM_DATA_ARRAY_LENGTH));

		dataInputStream = new ByteArrayInputStream(data);

		signature = asymmetricServiceForDefaultPolicy.sign(privateKeyForSigning, dataArray);

		keyPairConverter = asymmetricServiceForDefaultPolicy.getKeyPairConverter();

		emptyByteArray = new byte[0];
		emptyByteArrayArray = new byte[0][0];

		nullContentPublicKey = new TestPublicKey(null);
		emptyContentPublicKey = new TestPublicKey(emptyByteArray);

		nullContentPrivateKey = new TestPrivateKey(null);
		emptyContentPrivateKey = new TestPrivateKey(emptyByteArray);

		dataArrayConsistingOfNullElement = new byte[1][dataByteLength];
		dataArrayConsistingOfNullElement[0] = null;

		dataArrayConsistingOfEmptyElement = new byte[1][dataByteLength];
		dataArrayConsistingOfEmptyElement[0] = emptyByteArray;

		dataArrayContainingNullElement = dataArray.clone();
		dataArrayContainingNullElement[0] = null;

		dataArrayContainingEmptyElement = dataArray.clone();
		dataArrayContainingEmptyElement[0] = emptyByteArray;

		keyPair = AsymmetricTestDataGenerator.getIllegaSizeKeyPairForEncryption();
		illegalSizePublicKeyForEncryption = keyPair.getPublic();
		illegalSizePrivateKeyForDecryption = keyPair.getPrivate();

		keyPair = AsymmetricTestDataGenerator.getIllegalSizeKeyPairForSigning();
		illegalSizePublicKeyForVerification = keyPair.getPublic();
		illegalSizePrivateKeyForSigning = keyPair.getPrivate();
	}

	static Stream<Arguments> asymmetricallyEncrypt() {

		return Stream.of(arguments(null, data, "Public key is null."), arguments(nullContentPublicKey, data, "Public key content is null."),
				arguments(emptyContentPublicKey, data, "Public key content is empty."), arguments(illegalSizePublicKeyForEncryption, data,
						"Byte length of encryption public key must be equal to byte length of corresponding key in cryptographic policy for "
								+ "asymmetric service: "), arguments(publicKeyForEncryption, null, "Data is null."),
				arguments(publicKeyForEncryption, emptyByteArray, "Data is empty."));
	}

	static Stream<Arguments> asymmetricallyDecrypt() {

		return Stream.of(arguments(null, data, "Private key is null."), arguments(nullContentPrivateKey, data, "Private key content is null."),
				arguments(emptyContentPrivateKey, data, "Private key content is empty."), arguments(illegalSizePrivateKeyForDecryption, data,
						"Byte length of decryption private key must be equal to byte length of corresponding key in cryptographic policy for "
								+ "asymmetric service: "), arguments(privateKeyForDecryption, null, "Encrypted data is null."),
				arguments(privateKeyForDecryption, emptyByteArray, "Encrypted data is empty."));
	}

	static Stream<Arguments> sign() {

		return Stream
				.of(arguments(null, dataArray, "Private key is null."), arguments(nullContentPrivateKey, dataArray, "Private key content is null."),
						arguments(emptyContentPrivateKey, dataArray, "Private key content is empty."),
						arguments(illegalSizePrivateKeyForSigning, dataArray,
								"Byte length of signing private key must be equal to byte length of corresponding key in cryptographic policy for asymmetric"
										+ " service: "), arguments(privateKeyForSigning, null, "Data element array is null."),
						arguments(privateKeyForSigning, emptyByteArrayArray, "Data element array is empty."),
						arguments(privateKeyForSigning, dataArrayConsistingOfNullElement, "Data is null."),
						arguments(privateKeyForSigning, dataArrayConsistingOfEmptyElement, "Data is empty."),
						arguments(privateKeyForSigning, dataArrayContainingNullElement, "A data element is null."),
						arguments(privateKeyForSigning, dataArrayContainingEmptyElement, "A data element is empty."));
	}

	static Stream<Arguments> verifySignature() {

		return Stream.of(arguments(null, publicKeyForVerification, dataArray, "Signature is null."),
				arguments(emptyByteArray, publicKeyForVerification, dataArray, "Signature is empty."),
				arguments(signature, null, dataArray, "Public key is null."),
				arguments(signature, nullContentPublicKey, dataArray, "Public key content is null."),
				arguments(signature, emptyContentPublicKey, dataArray, "Public key content is empty."),
				arguments(signature, illegalSizePublicKeyForVerification, dataArray,
						"Byte length of signature verification public key must be equal to byte length of corresponding key in cryptographic policy "
								+ "for asymmetric service: "), arguments(signature, publicKeyForVerification, null, "Data element array is null."),
				arguments(signature, publicKeyForVerification, emptyByteArrayArray, "Data element array is empty."),
				arguments(signature, publicKeyForVerification, dataArrayConsistingOfNullElement, "Data is null."),
				arguments(signature, publicKeyForVerification, dataArrayConsistingOfEmptyElement, "Data is empty."),
				arguments(signature, publicKeyForVerification, dataArrayContainingNullElement, "A data element is null."),
				arguments(signature, publicKeyForVerification, dataArrayContainingEmptyElement, "A data element is empty."));
	}

	static Stream<Arguments> signFromDataInputStream() {

		return Stream.of(arguments(null, dataInputStream, "Private key is null."),
				arguments(nullContentPrivateKey, dataInputStream, "Private key content is null."),
				arguments(emptyContentPrivateKey, dataInputStream, "Private key content is empty."),
				arguments(illegalSizePrivateKeyForSigning, dataInputStream,
						"Byte length of signing private key must be equal to byte length of corresponding key in cryptographic policy for asymmetric"
								+ " service: "), arguments(privateKeyForSigning, null, "Data input stream is null."));
	}

	static Stream<Arguments> verifySignatureFromDataInputStream() {

		return Stream.of(arguments(null, publicKeyForVerification, dataInputStream, "Signature is null."),
				arguments(emptyByteArray, publicKeyForVerification, dataInputStream, "Signature is empty."),
				arguments(signature, null, dataInputStream, "Public key is null."),
				arguments(signature, nullContentPublicKey, dataInputStream, "Public key content is null."),
				arguments(signature, emptyContentPublicKey, dataInputStream, "Public key content is empty."),
				arguments(signature, illegalSizePublicKeyForVerification, dataInputStream,
						"Byte length of signature verification public key must be equal to byte length of corresponding key in cryptographic policy "
								+ "for asymmetric service: "), arguments(signature, publicKeyForVerification, null, "Data input stream is null."));
	}

	static Stream<Arguments> convertPublicKeyForEncryptionToPem() {

		return Stream.of(arguments(null, "Public key is null."), arguments(nullContentPublicKey, "Public key content is null."),
				arguments(emptyContentPublicKey, "Public key content is empty."));
	}

	static Stream<Arguments> convertPrivateKeyForEncryptionToPem() {

		return Stream.of(arguments(null, "Private key is null."), arguments(nullContentPrivateKey, "Private key content is null."),
				arguments(emptyContentPrivateKey, "Private key content is empty."));
	}

	static Stream<Arguments> getPublicKeyForEncryptionFromPem() {

		return Stream.of(arguments(null, "Public key PEM string is null."), arguments("", "Public key PEM string is blank."),
				arguments(whiteSpaceString, "Public key PEM string is blank."));
	}

	static Stream<Arguments> getPrivateKeyForEncryptionFromPem() {

		return Stream.of(arguments(null, "Private key PEM string is null."), arguments("", "Private key PEM string is blank."),
				arguments(whiteSpaceString, "Private key PEM string is blank."));
	}

	static Stream<Arguments> convertPublicKeyForSigningToPem() {

		return Stream.of(arguments(null, "Public key is null."), arguments(nullContentPublicKey, "Public key content is null."),
				arguments(emptyContentPublicKey, "Public key content is empty."));
	}

	static Stream<Arguments> convertPrivateKeyForSigningToPem() {

		return Stream.of(arguments(null, "Private key is null."), arguments(nullContentPrivateKey, "Private key content is null."),
				arguments(emptyContentPrivateKey, "Private key content is empty."));
	}

	static Stream<Arguments> getPublicKeyForSigningFromPem() {

		return Stream.of(arguments(null, "Public key PEM string is null."), arguments("", "Public key PEM string is blank."),
				arguments(whiteSpaceString, "Public key PEM string is blank."));
	}

	static Stream<Arguments> getPrivateKeyForSigningFromPem() {

		return Stream.of(arguments(null, "Private key PEM string is null."), arguments("", "Private key PEM string is blank."),
				arguments(whiteSpaceString, "Private key PEM string is blank."));
	}

	@ParameterizedTest
	@MethodSource("asymmetricallyEncrypt")
	void testAsymmetricEncryptionValidation(final PublicKey key, final byte[] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.encrypt(key, data));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("asymmetricallyDecrypt")
	void testAsymmetricDecryptionValidation(final PrivateKey key, final byte[] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.decrypt(key, data));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("sign")
	void testSigningValidation(final PrivateKey key, final byte[][] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.sign(key, data));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("verifySignature")
	void testSignatureVerificationValidation(final byte[] signature, final PublicKey key, final byte[][] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.verifySignature(signature, key, data));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("signFromDataInputStream")
	void testSigningFromDataInputStreamValidation(final PrivateKey key, final InputStream inStream, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.sign(key, inStream));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("verifySignatureFromDataInputStream")
	void testSignatureVerificationFromDataInputStreamValidation(final byte[] signature, final PublicKey key, final InputStream inStream, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> asymmetricServiceForDefaultPolicy.verifySignature(signature, key, inStream));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("convertPublicKeyForEncryptionToPem")
	void testPublicKeyForEncryptionConversionToPemValidation(final PublicKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.exportPublicKeyForEncryptingToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("convertPrivateKeyForEncryptionToPem")
	void testPrivateKeyForEncryptionConversionToPemValidation(final PrivateKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.exportPrivateKeyForEncryptingToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPublicKeyForEncryptionFromPem")
	void testPublicKeyForEncryptionRetrievalFromPemValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.getPublicKeyForEncryptingFromPem(pemStr));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPrivateKeyForEncryptionFromPem")
	void testPrivateKeyForEncryptionRetrievalFromPemValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.getPrivateKeyForEncryptingFromPem(pemStr));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("convertPublicKeyForSigningToPem")
	void testPublicKeyForSigningConversionToPemValidation(final PublicKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.exportPublicKeyForSigningToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("convertPrivateKeyForSigningToPem")
	void testPrivateKeyForSigningConversionToPemValidation(final PrivateKey key, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.exportPrivateKeyForSigningToPem(key));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPublicKeyForSigningFromPem")
	void testPublicKeyForSigningRetrievalFromPemValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.getPublicKeyForSigningFromPem(pemStr));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPrivateKeyForSigningFromPem")
	void testGetPrivateKeyForSigningRetrievalFromPemValidation(final String pemStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> keyPairConverter.getPrivateKeyForEncryptingFromPem(pemStr));
		assertEquals(errorMsg, exception.getMessage());
	}

}
