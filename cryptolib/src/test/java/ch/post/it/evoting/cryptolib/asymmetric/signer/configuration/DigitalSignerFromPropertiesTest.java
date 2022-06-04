/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DigitalSignerFromPropertiesTest {

	private static final int PADDING_SALT_BIT_LENGTH = 32;
	private static final int PADDING_TRAILER_FIELD = 1;

	private static DigitalSignerPolicyFromProperties signerPolicyFromProperties;

	@BeforeAll
	static void setUp() {
		signerPolicyFromProperties = new DigitalSignerPolicyFromProperties();
	}

	@Test
	void givenPolicyWhenGetDigitalSignerConfigThenExpectedValues() {
		final ConfigDigitalSignerAlgorithmAndSpec config = signerPolicyFromProperties.getDigitalSignerAlgorithmAndSpec();

		final boolean algorithmAndPaddingCorrect = config.getAlgorithmAndPadding().equals("SHA256withRSA/PSS");
		final boolean paddingMessageDigestAlgorithmCorrect = config.getPaddingMessageDigestAlgorithm().equals("SHA-256");
		final boolean paddingMaskingGenerationFunctionAlgorithmCorrect = config.getPaddingInfo().getPaddingMaskingGenerationFunctionAlgorithm()
				.equals("MGF1");
		final boolean paddingMaskingGenerationFunctionMessageDigestAlgorithmCorrect = config.getPaddingInfo()
				.getPaddingMaskingGenerationFunctionMessageDigestAlgorithm().equals("SHA-256");
		final boolean paddingSaltBitLengthCorrect = config.getPaddingInfo().getPaddingSaltBitLength() == PADDING_SALT_BIT_LENGTH;
		final boolean paddingTrailerFieldCorrect = config.getPaddingInfo().getPaddingTrailerField() == PADDING_TRAILER_FIELD;

		assertTrue(algorithmAndPaddingCorrect && paddingMessageDigestAlgorithmCorrect && paddingMaskingGenerationFunctionAlgorithmCorrect
				&& paddingMaskingGenerationFunctionMessageDigestAlgorithmCorrect && paddingSaltBitLengthCorrect && paddingTrailerFieldCorrect);
	}
}
