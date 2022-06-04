/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.ConfigMacAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicy;

class MacFactoryTest {

	@Test
	void whenCreateMacFactoryUsingPolicyHmac256AndSun() {
		final MacFactory macFactory = new MacFactory(getMacPolicyHmac256AndSun());

		assertNotNull(macFactory.create());
	}

	@Test
	void whenCreateMacGeneratorUsingPolicyHmac256AndBc() {
		final MacFactory acFactory = new MacFactory(getMacPolicyHmac256AndBc());

		assertNotNull(acFactory.create());
	}

	private MacPolicy getMacPolicyHmac256AndSun() {
		return () -> ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_SUN;
	}

	private MacPolicy getMacPolicyHmac256AndBc() {
		return () -> ConfigMacAlgorithmAndProvider.HMAC_WITH_SHA256_BC;
	}
}
