/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class AsymmetricCipherFromPropertiesTest {

	private static AsymmetricCipherPolicyFromProperties cipherPolicyFromProperties;

	@BeforeAll
	public static void setUp() {

		cipherPolicyFromProperties = new AsymmetricCipherPolicyFromProperties();
	}

	@Test
	void givenPolicyWhenGetSymmetricCipherConfigThenExpectedValues() {

		final ConfigAsymmetricCipherAlgorithmAndSpec config = cipherPolicyFromProperties.getAsymmetricCipherAlgorithmAndSpec();

		Assertions.assertEquals("RSA/RSA-KEMWITHKDF1ANDSHA-256/NOPADDING", config.getAlgorithmModePadding());
		Assertions.assertEquals(Provider.BOUNCY_CASTLE, config.getProvider());
	}
}
