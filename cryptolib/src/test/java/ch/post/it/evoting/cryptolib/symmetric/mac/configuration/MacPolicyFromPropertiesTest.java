/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MacPolicyFromPropertiesTest {

	private static MacPolicyFromProperties macPolicyFromProperties;

	@BeforeAll
	static void setUp() {
		macPolicyFromProperties = new MacPolicyFromProperties();
	}

	@Test
	void givenPolicyWhenGetMacConfigThenExpectedValues() {
		final ConfigMacAlgorithmAndProvider config = macPolicyFromProperties.getMacAlgorithmAndProvider();

		assertEquals("HmacSHA256", config.getAlgorithm());
	}
}
