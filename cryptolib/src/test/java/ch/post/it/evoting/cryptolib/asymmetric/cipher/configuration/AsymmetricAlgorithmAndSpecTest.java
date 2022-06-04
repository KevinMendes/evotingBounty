/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class AsymmetricAlgorithmAndSpecTest {

	@Test
	void givenAlgorithmModePaddingWhenSpecIsRsaWithRsaKemAndKdf1AndSha256AndBc() {

		assertAlgorithmModePadding("RSA/RSA-KEMWITHKDF1ANDSHA-256/NOPADDING",
				ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC);
	}

	@Test
	void givenAlgorithmModePaddingWhenSpecIsRsaWithRsaKemAndKdf2AndSha256AndBc() {

		assertAlgorithmModePadding("RSA/RSA-KEMWITHKDF2ANDSHA-256/NOPADDING",
				ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_BC);
	}

	@Test
	void givenProviderWhenSpecIsRsaWithRsaKemAndKdf1AndSha256AndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC);
	}

	@Test
	void givenProviderWhenSpecIsRsaWithRsaKemAndKdf2AndSha256AndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_BC);
	}

	private void assertAlgorithmModePadding(final String expectedAlgorithm, final ConfigAsymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec) {

		Assertions.assertEquals(expectedAlgorithm, cipherAlgorithmAndSpec.getAlgorithmModePadding(),
				"Algorithm/mode/padding was not the expected value.");
	}

	private void assertProvider(final Provider expectedProvider, final ConfigAsymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec) {

		Assertions.assertEquals(expectedProvider, cipherAlgorithmAndSpec.getProvider(), "Provider was not the expected value.");
	}
}
