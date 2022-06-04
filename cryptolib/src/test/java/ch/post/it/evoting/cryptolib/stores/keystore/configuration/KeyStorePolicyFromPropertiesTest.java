/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class KeyStorePolicyFromPropertiesTest {

	private static KeyStorePolicyFromProperties keyStorePolicyFromProperties;

	@BeforeAll
	public static void setUp() {
		keyStorePolicyFromProperties = new KeyStorePolicyFromProperties();
	}

	@Test
	void givenKeyStorePolicyWhenGetSpecThenExpectedValues() {

		final ConfigKeyStoreSpec config = keyStorePolicyFromProperties.getKeyStoreSpec();

		Assertions.assertEquals(Provider.SUN, config.getProvider());
	}
}
