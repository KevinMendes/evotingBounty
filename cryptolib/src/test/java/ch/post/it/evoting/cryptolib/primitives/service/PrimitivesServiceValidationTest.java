/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.service;

import static java.util.Arrays.fill;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomInteger;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class PrimitivesServiceValidationTest {

	private static PrimitivesService primitivesServiceForDefaultPolicy;
	private static String whiteSpaceString;
	private static CryptoAPIRandomInteger cryptoRandomInteger;
	private static CryptoAPIRandomString crypto32CharRandomString;
	private static CryptoAPIRandomString crypto64CharRandomString;
	private static CryptoAPIKDFDeriver cryptoKdfDeriver;
	private static CryptoAPIPBKDFDeriver cryptoPbkdfDeriver;
	private static int kdfLengthInBytes;
	private static byte[] kdfSeed;
	private static byte[] pbkdfSalt;
	private static byte[] emptyByteArray;
	private static char[] emptyCharArray;
	private static int aboveMaxByteArrayLength;
	private static int aboveMaxBigIntegerDigitLength;
	private static int belowMinBigIntegerDigitLength;
	private static int aboveMaxStringLength;
	private static String belowMinLengthPbKdfPassword;
	private static String aboveMaxLengthPbKdfPassword;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		primitivesServiceForDefaultPolicy = new PrimitivesService();

		final char[] whiteSpaceChars = new char
				[CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH)];
		fill(whiteSpaceChars, ' ');
		whiteSpaceString = new String(whiteSpaceChars);

		cryptoRandomInteger = primitivesServiceForDefaultPolicy.getCryptoRandomInteger();
		crypto32CharRandomString = primitivesServiceForDefaultPolicy.get32CharAlphabetCryptoRandomString();
		crypto64CharRandomString = primitivesServiceForDefaultPolicy.get64CharAlphabetCryptoRandomString();
		cryptoKdfDeriver = primitivesServiceForDefaultPolicy.getKDFDeriver();
		cryptoPbkdfDeriver = primitivesServiceForDefaultPolicy.getPBKDFDeriver();

		kdfLengthInBytes = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH);
		kdfSeed = primitivesServiceForDefaultPolicy
				.genRandomBytes(CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH));

		pbkdfSalt = primitivesServiceForDefaultPolicy.genRandomBytes(CommonTestDataGenerator
				.getInt(DerivationConstants.MINIMUM_PBKDF_SALT_LENGTH_IN_BYTES, SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH));

		emptyByteArray = new byte[0];
		emptyCharArray = new char[0];

		aboveMaxByteArrayLength = SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH + 1;
		aboveMaxBigIntegerDigitLength = SecureRandomConstants.MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH + 1;
		belowMinBigIntegerDigitLength = SecureRandomConstants.MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH - 1;
		aboveMaxStringLength = SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH + 1;

		belowMinLengthPbKdfPassword = crypto64CharRandomString
				.nextRandom(CommonTestDataGenerator.getInt(1, (DerivationConstants.MINIMUM_PBKDF_PASSWORD_LENGTH - 1)));
		aboveMaxLengthPbKdfPassword = CommonTestDataGenerator.getAlphanumeric(DerivationConstants.MAXIMUM_PBKDF_PASSWORD_LENGTH + 1);
	}

	static Stream<Arguments> generateHash() {
		return Stream.of(arguments(null, "Data is null."), arguments(emptyByteArray, "Data is empty."));
	}

	static Stream<Arguments> generateHashForDataInputStream() {
		return Stream.of(arguments(null, "Data input stream is null."));
	}

	static Stream<Arguments> generateRandomBytes() {

		return Stream.of(arguments(0, "Length in bytes must be greater than or equal to : 1; Found 0"), arguments(aboveMaxByteArrayLength,
				"Length in bytes must be less than or equal to maximum allowed value for secure random byte arrays: "
						+ SecureRandomConstants.MAXIMUM_GENERATED_BYTE_ARRAY_LENGTH + "; Found " + aboveMaxByteArrayLength));
	}

	static Stream<Arguments> generateRandomBigInteger() {
		return Stream.of(arguments(belowMinBigIntegerDigitLength,
						String.format("Length in digits must be greater than or equal to the minimum allowed number of digits (%s). Provided length: %s.",
								SecureRandomConstants.MINIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH, belowMinBigIntegerDigitLength)),
				arguments(aboveMaxBigIntegerDigitLength, String.format(
						"Length in digits must be smaller than or equal to the maximum allowed number of digits (%s). Provided length: %s.",
						SecureRandomConstants.MAXIMUM_GENERATED_BIG_INTEGER_DIGIT_LENGTH, aboveMaxBigIntegerDigitLength)));
	}

	static Stream<Arguments> generateRandomBigIntegerByBits() {
		final int length = 0;
		return Stream.of(arguments(length, String.format("Length in bits must be positive. Provided length: %s.", length)));
	}

	static Stream<Arguments> generate32CharRandomString() {

		return Stream.of(arguments(0, "Length in characters must be greater than or equal to : 1; Found 0"), arguments(aboveMaxStringLength,
				"Length in characters must be less than or equal to maximum allowed value for secure random Strings: "
						+ SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH + "; Found " + aboveMaxStringLength));
	}

	static Stream<Arguments> generate64CharRandomString() {

		return Stream.of(arguments(0, "Length in characters must be greater than or equal to : 1; Found 0"), arguments(aboveMaxStringLength,
				"Length in characters must be less than or equal to maximum allowed value for secure random Strings: "
						+ SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH + "; Found " + aboveMaxStringLength));
	}

	static Stream<Arguments> deriveKdf() {

		return Stream.of(arguments(null, kdfLengthInBytes, "Seed is null."), arguments(emptyByteArray, kdfLengthInBytes, "Seed is empty."),
				arguments(kdfSeed, 0, "Length in bytes must be a positive integer; Found 0"));
	}

	static Stream<Arguments> derivePbkdfWithSalt() {

		return Stream.of(arguments(null, pbkdfSalt, "Password is null."), arguments(emptyCharArray, pbkdfSalt, "Password is blank."),
				arguments(whiteSpaceString.toCharArray(), pbkdfSalt, "Password is blank."),
				arguments(belowMinLengthPbKdfPassword.toCharArray(), pbkdfSalt,
						"Password length must be greater than or equal to : " + DerivationConstants.MINIMUM_PBKDF_PASSWORD_LENGTH + "; Found "
								+ belowMinLengthPbKdfPassword.length()), arguments(aboveMaxLengthPbKdfPassword.toCharArray(), pbkdfSalt,
						"Password length must be less than or equal to : " + DerivationConstants.MAXIMUM_PBKDF_PASSWORD_LENGTH + "; Found "
								+ aboveMaxLengthPbKdfPassword.length()));
	}

	@ParameterizedTest
	@MethodSource("generateHash")
	void testHashGenerationValidation(final byte[] data, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> primitivesServiceForDefaultPolicy.getHash(data));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateHashForDataInputStream")
	void testHashGenerationForDataInputStreamValidation(final InputStream inStream, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> primitivesServiceForDefaultPolicy.getHash(inStream));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateRandomBytes")
	void testRandomBytesGenerationValidation(final int byteLength, final String errorMsg) {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> primitivesServiceForDefaultPolicy.genRandomBytes(byteLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateRandomBigInteger")
	void testRandomBigIntegerGenerationValidation(final int digitLength, final String errorMsg) {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> cryptoRandomInteger.genRandomIntegerByDigits(digitLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generateRandomBigIntegerByBits")
	void testRandomBigIntegerGenerationByBitsValidation(final int bitLength, final String errorMsg) {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> cryptoRandomInteger.genRandomIntegerByBits(bitLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generate32CharRandomString")
	void test32CharRandomStringGenerationValidation(final int charLength, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> crypto32CharRandomString.nextRandom(charLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("generate64CharRandomString")
	void test64CharRandomStringGenerationValidation(final int charLength, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> crypto64CharRandomString.nextRandom(charLength));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("deriveKdf")
	void testKdfDerivationValidation(final byte[] seed, final int lengthInBytes, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> cryptoKdfDeriver.deriveKey(seed, lengthInBytes));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("derivePbkdfWithSalt")
	void testPbkdfDerivationWithSaltValidation(final char[] password, final byte[] salt, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> cryptoPbkdfDeriver.deriveKey(password, salt));
		assertEquals(errorMsg, exception.getMessage());
	}

}
