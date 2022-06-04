/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class DigitalSignerAlgorithmAndSpecTest {

	private static final int PADDING_SALT_BIT_LENGTH = 32;

	private static final int PADDING_TRAILER_FIELD = 1;

	@Test
	void givenAlgorithmWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals("SHA256withRSA/PSS",
				ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getAlgorithmAndPadding(),
				"Algorithm/padding was not the expected value");
	}

	@Test
	void givenPaddingMessageDigestAlgorithmWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals("SHA-256",
				ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getPaddingMessageDigestAlgorithm(),
				"Padding message digest algorithm was not the expected value");
	}

	@Test
	void givenPaddingMaskingFunctionAlgorithmWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals("MGF1", ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getPaddingInfo()
				.getPaddingMaskingGenerationFunctionAlgorithm(), "Padding masking generation function algorithm was not the expected value");
	}

	@Test
	void givenPaddingMaskingFunctionMessageDigestAlgorithmWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals("SHA-256", ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getPaddingInfo()
						.getPaddingMaskingGenerationFunctionMessageDigestAlgorithm(),
				"Padding masking generation function message digest algorithm was not the expected value");
	}

	@Test
	void givenPaddingSaltLengthWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals(PADDING_SALT_BIT_LENGTH,
				ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getPaddingInfo().getPaddingSaltBitLength(),
				"Padding salt bit length was not the expected value");
	}

	@Test
	void givenPaddingTrailerFieldWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals(PADDING_TRAILER_FIELD,
				ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getPaddingInfo().getPaddingTrailerField(),
				"Padding trailer field was not the expected value");
	}

	@Test
	void givenProviderWhenSpecIsSha256WithRsaAndPssAndBc() {

		Assertions.assertEquals(Provider.BOUNCY_CASTLE,
				ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC.getProvider(),
				"Provider was not the expected value");
	}
}
