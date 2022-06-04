/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SymmetricCipherFromPropertiesTest {

	private static final int INIT_VECTOR_BIT_LENGTH = 96;
	private static final int AUTHENTICATION_TAG_BIT_LENGTH = 128;

	private static SymmetricCipherPolicyFromProperties cipherPolicyFromProperties;

	@BeforeAll
	static void setUp() {
		cipherPolicyFromProperties = new SymmetricCipherPolicyFromProperties();
	}

	@Test
	void givenPolicyWhenGetSymmetricCipherConfigThenExpectedValues() {
		final ConfigSymmetricCipherAlgorithmAndSpec config = cipherPolicyFromProperties.getSymmetricCipherAlgorithmAndSpec();

		final boolean algorithmAndModeAndPaddingCorrect = config.getTransformation().equals("AES/GCM/NoPadding");
		final boolean initVectorLengthCorrect = config.getInitVectorBitLength() == INIT_VECTOR_BIT_LENGTH;
		final boolean authTagLengthCorrect = config.getAuthTagBitLength() == AUTHENTICATION_TAG_BIT_LENGTH;

		assertTrue(algorithmAndModeAndPaddingCorrect && initVectorLengthCorrect && authTagLengthCorrect);
	}
}
