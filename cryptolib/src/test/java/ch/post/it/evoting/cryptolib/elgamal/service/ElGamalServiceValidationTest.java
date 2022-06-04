/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalEncryptionParameters;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class ElGamalServiceValidationTest {

	private static String whiteSpaceString;

	@BeforeAll
	static void setUp() {
		whiteSpaceString = CommonTestDataGenerator
				.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH));

	}

	static Stream<Arguments> deserializeElGamalEncryptionParameters() throws SecurityException, IllegalArgumentException {

		final String encryptionParamsWithNullP = "{\"encryptionParams\":{\"g\":\"Ag==\",\"p\":null,\"q\":\"Cw==\"}}";
		final String encryptionParamsWithNullQ = "{\"encryptionParams\":{\"g\":\"Ag==\",\"p\":\"Fw==\",\"q\":null}}";
		final String encryptionParamsWithNullG = "{\"encryptionParams\":{\"g\":null,\"p\":\"Fw==\",\"q\":\"Cw==\"}}";
		final String encryptionParamsWithIncorrectPQRelationship = "{\"encryptionParams\":{\"g\":\"Ag==\",\"q\":\"Fw==\",\"p\":\"Cw==\"}}";
		final String encryptionParamsWithIncorrectPGRelationship = "{\"encryptionParams\":{\"g\":\"Fw==\",\"p\":\"Fw==\",\"q\":\"Cw==\"}}";

		return Stream.of(arguments(null, ElGamalEncryptionParameters.class.getSimpleName() + " JSON string is null."),
				arguments("", ElGamalEncryptionParameters.class.getSimpleName() + " JSON string is blank."),
				arguments(whiteSpaceString, ElGamalEncryptionParameters.class.getSimpleName() + " JSON string is blank."),
				arguments(encryptionParamsWithNullP, "Zp subgroup p parameter is null."),
				arguments(encryptionParamsWithNullQ, "Zp subgroup q parameter is null."),
				arguments(encryptionParamsWithNullG, "Zp subgroup generator is null."), arguments(encryptionParamsWithIncorrectPQRelationship,
						"Zp subgroup q parameter must be less than or equal to Zp subgroup p parameter minus 1"),
				arguments(encryptionParamsWithIncorrectPGRelationship,
						"Zp subgroup generator must be less than or equal to Zp subgroup p parameter minus 1"));
	}

	static Stream<Arguments> deserializeElGamalPublicKey() {

		final String publicKeyWithNullZpSubgroup = "{\"publicKey\":{\"zpSubgroup\":null,\"elements\":[\"Ag==\",\"Ag==\"]}}";
		final String publicKeyWithNullElementList = "{\"publicKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"},\"elements\":null}}";
		final String publicKeyWithEmptyElementList = "{\"publicKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"},\"elements\":[]}}";
		final String publicKeyWithNullInElementList =
				"{\"publicKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"}," + "\"elements\":[\"Ag==\",null]}}";

		return Stream.of(arguments(null, ElGamalPublicKey.class.getSimpleName() + " JSON string is null."),
				arguments("", ElGamalPublicKey.class.getSimpleName() + " JSON string is blank."),
				arguments(whiteSpaceString, ElGamalPublicKey.class.getSimpleName() + " JSON string is blank."),
				arguments(publicKeyWithNullZpSubgroup, "Zp subgroup is null."),
				arguments(publicKeyWithNullElementList, "List of ElGamal public key elements is null."),
				arguments(publicKeyWithEmptyElementList, "List of ElGamal public key elements is empty."),
				arguments(publicKeyWithNullInElementList, "List of ElGamal public key elements contains one or more null elements."));
	}

	static Stream<Arguments> deserializeElGamalPrivateKey() {

		final String privateKeyWithNullZpSubgroup = "{\"privateKey\":{\"zpSubgroup\":null,\"exponents\":[\"BA==\",\"BQ==\"]}}";
		final String privateKeyWithNullElementList = "{\"privateKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"},\"exponents\":null}}";
		final String privateKeyWithEmptyElementList = "{\"privateKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"},\"exponents\":[]}}";
		final String privateKeyWithNullInElementList =
				"{\"privateKey\":{\"zpSubgroup\":{\"p\":\"Fw==\",\"q\":\"Cw==\",\"g\":\"Ag==\"}," + "\"exponents\":[\"BA==\",null]}}";

		return Stream.of(arguments(null, ElGamalPrivateKey.class.getSimpleName() + " JSON string is null."),
				arguments("", ElGamalPrivateKey.class.getSimpleName() + " JSON string is blank."),
				arguments(whiteSpaceString, ElGamalPrivateKey.class.getSimpleName() + " JSON string is blank."),
				arguments(privateKeyWithNullZpSubgroup, "Zp subgroup is null."),
				arguments(privateKeyWithNullElementList, "List of ElGamal private key exponents is null."),
				arguments(privateKeyWithEmptyElementList, "List of ElGamal private key exponents is empty."),
				arguments(privateKeyWithNullInElementList, "List of ElGamal private key exponents contains one or more null elements."));
	}

	@ParameterizedTest
	@MethodSource("deserializeElGamalEncryptionParameters")
	void testElGamalEncryptionParametersDeserializationValidation(final String jsonStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> ElGamalEncryptionParameters.fromJson(jsonStr));
		assertTrue(exception.getMessage().contains(errorMsg));
	}

	@ParameterizedTest
	@MethodSource("deserializeElGamalPublicKey")
	void testElGamalPublicKeyDeserializationValidation(final String jsonStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> ElGamalPublicKey.fromJson(jsonStr));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("deserializeElGamalPrivateKey")
	void testElGamalPrivateKeyDeserializationValidation(final String jsonStr, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> ElGamalPrivateKey.fromJson(jsonStr));
		assertEquals(errorMsg, exception.getMessage());
	}
}
