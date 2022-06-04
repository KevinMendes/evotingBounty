/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

class ConfigKeyStoreSpecTest {

	@Test
	void givenKeyStoreSpecForSunThenProviderIsSun() {

		assertProvider(Provider.SUN, ConfigKeyStoreSpec.SUN);
	}

	private void assertProvider(final Provider expectedProvider, final ConfigKeyStoreSpec configKeyStoreSpec) {

		Assertions.assertEquals(expectedProvider, configKeyStoreSpec.getProvider(), "Provider name does not have the expected value");
	}
}
