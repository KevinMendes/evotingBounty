/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;

class SecureRandomAlgorithmProviderPairTest {

	@Test
	void givenNativePrngSunWhenGetNameThenExpectedValues() {
		assertAlgorithm("NativePRNG", ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN);
	}

	@Test
	void givenWindowsPrngSunMSCAPIWhenGetNameThenExpectedValues() {
		assertAlgorithm("Windows-PRNG", ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI);
	}

	@Test
	void givenNativePrngSunWhenGetProviderThenExpectedValues() {
		assertProvider(Provider.SUN, ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN);
	}

	@Test
	void givenWindowsPrngSunMSCAPIWhenGetProviderThenExpectedValues() {
		assertProvider(Provider.SUN_MSCAPI, ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI);
	}

	@Test
	void givenNativePrngSunWhenCheckingOSCompliance() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		assertTrue(() -> ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN.isOSCompliant(OperatingSystem.UNIX));
	}

	@Test
	void givenWindowsPrngSunMSCAPIWhenCheckingOSCompliance() {
		assumeTrue(OperatingSystem.WINDOWS.isCurrent());

		assertTrue(() -> ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI.isOSCompliant(OperatingSystem.WINDOWS));
	}

	private void assertAlgorithm(final String expectedAlgorithm, final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider) {
		assertEquals(expectedAlgorithm, secureRandomAlgorithmAndProvider.getAlgorithm(), "Algorithm name was not the expected value");
	}

	private void assertProvider(final Provider expectedProvider, final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider) {
		assertEquals(expectedProvider, secureRandomAlgorithmAndProvider.getProvider(), "Provider was not the expected value");
	}
}
