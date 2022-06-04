/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class KeyPairPolicyFromPropertiesTest {

	private static KeyPairPolicyFromProperties keyPairPolicyFromProperties;

	@BeforeAll
	static void setUp() {
		keyPairPolicyFromProperties = new KeyPairPolicyFromProperties();
	}

	@Test
	void whenGetSigningKeyAlgorithmAndSpecThenExpectedValue() {
		final String errorMsg = "The returned signing key algorithm and spec type is not the expected value";

		assertEquals(ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN, keyPairPolicyFromProperties.getSigningKeyPairAlgorithmAndSpec(),
				errorMsg);
	}

	@Test
	void whenGetEncryptionKeyAlgorithmAndSpecThenExpectedValue() {
		final String errorMsg = "The returned encryption key algorithm and spec type is not the expected value";

		assertEquals(ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN,
				keyPairPolicyFromProperties.getEncryptingKeyPairAlgorithmAndSpec(), errorMsg);
	}

	@Test
	void whenGetSecureRandomThenExpectedValue() {
		final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
		if (OperatingSystem.WINDOWS.isCurrent()) {
			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
		} else {
			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
		}

		final String errorMsg = "The returned secure random algorithm and provider type is not the expected value";

		assertEquals(secureRandomAlgorithmAndProvider, keyPairPolicyFromProperties.getSecureRandomAlgorithmAndProvider(), errorMsg);
	}
}
