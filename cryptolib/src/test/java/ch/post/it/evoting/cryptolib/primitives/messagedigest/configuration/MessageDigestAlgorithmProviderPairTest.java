/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class MessageDigestAlgorithmProviderPairTest {

	@Test
	void givenAlgorithmSha256WhenPairIsSha256AndSun() {

		assertAlgorithm("SHA-256", ConfigMessageDigestAlgorithmAndProvider.SHA256_SUN);
	}

	@Test
	void givenAlgorithmSha256WhenPairIsSha256AndBc() {

		assertAlgorithm("SHA-256", ConfigMessageDigestAlgorithmAndProvider.SHA256_BC);
	}

	@Test
	void givenAlgorithmSha512With224WhenPairIsSha512With224AndBc() {

		assertAlgorithm("SHA-512/224", ConfigMessageDigestAlgorithmAndProvider.SHA512_224_BC);
	}

	@Test
	void givenProviderSunWhenPairIsSha256AndSun() {

		assertProvider(Provider.SUN, ConfigMessageDigestAlgorithmAndProvider.SHA256_SUN);
	}

	@Test
	void givenProviderBcWhenPairIsSha256AndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigMessageDigestAlgorithmAndProvider.SHA256_BC);
	}

	@Test
	void givenProviderBcWhenPairIsSha512With224AndBc() {

		assertProvider(Provider.BOUNCY_CASTLE, ConfigMessageDigestAlgorithmAndProvider.SHA512_224_BC);
	}

	private void assertAlgorithm(final String expectedAlgorithm, final ConfigMessageDigestAlgorithmAndProvider MessageDigestAlgorithmAndProvider) {

		Assertions.assertEquals(expectedAlgorithm, MessageDigestAlgorithmAndProvider.getAlgorithm(), "Algorithm name was not the expected value");
	}

	private void assertProvider(final Provider expectedProvider, final ConfigMessageDigestAlgorithmAndProvider MessageDigestAlgorithmAndProvider) {

		Assertions.assertEquals(expectedProvider, MessageDigestAlgorithmAndProvider.getProvider(), "Provider was not the expected value");
	}
}
