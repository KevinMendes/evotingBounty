/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class SymmetricCipherAlgorithmAndSpecTest {

	private static final int GCM_AUTHENTICATION_TAG_BIT_LENGTH = 128;

	@Test
	void givenAlgorithmAesGcmNoPaddingWhenSpecIsAesGcmNoPaddingAndBc() {

		assertAlgorithmModePadding("AES/GCM/NoPadding", ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC);
	}

	@Test
	void givenExpectedAuthTagLengthWhenSpecIsAesGcmNoPaddingAndBc() {

		assertAuthenticationTagLength(GCM_AUTHENTICATION_TAG_BIT_LENGTH, ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC);
	}

	@Test
	void givenProviderBcWhenSpecIsAesGcmNoPaddingAndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC);
	}

	private void assertAlgorithmModePadding(final String expectedAlgorithm, final ConfigSymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec) {

		Assertions.assertEquals(expectedAlgorithm, cipherAlgorithmAndSpec.getTransformation(), "Algorithm/mode/padding was not the expected value");
	}

	private void assertAuthenticationTagLength(final int expectedAuthTagLength, final ConfigSymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec) {

		Assertions.assertEquals(expectedAuthTagLength, cipherAlgorithmAndSpec.getAuthTagBitLength(),
				"Authentication tag length was not the expected value");
	}

	private void assertProvider(final Provider expectedProvider, final ConfigSymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec) {

		Assertions.assertEquals(expectedProvider, cipherAlgorithmAndSpec.getProvider(), "Provider was not the expected value");
	}
}
