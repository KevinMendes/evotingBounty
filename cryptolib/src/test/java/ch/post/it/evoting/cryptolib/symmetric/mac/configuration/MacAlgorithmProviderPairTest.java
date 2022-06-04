/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class MacAlgorithmProviderPairTest {

	@Test
	void givenAlgorithmSha256WhenPairIsHmac256AndSun() {

		assertAlgorithm("HmacSHA256", ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_SUN);
	}

	@Test
	void givenAlgorithmSha256WhenPairIsHmac256AndBc() {

		assertAlgorithm("HmacSHA256", ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_BC);
	}

	@Test
	void givenProviderSunWhenPairIsHmac256AndSun() {

		assertProvider(Provider.SUN_JCE, ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_SUN);
	}

	@Test
	void givenProviderBcWhenPairIsHmac256AndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_BC);
	}

	private void assertAlgorithm(final String expectedAlgorithm, final ConfigMacAlgorithmAndProvider macAlgorithmAndProvider) {

		Assertions.assertEquals(expectedAlgorithm, macAlgorithmAndProvider.getAlgorithm(), "Algorithm name was not the expected value");
	}

	private void assertProvider(final Provider expectedProvider, final ConfigMacAlgorithmAndProvider macAlgorithmAndProvider) {

		Assertions.assertEquals(expectedProvider, macAlgorithmAndProvider.getProvider(), "Provider was not the expected value");
	}
}
