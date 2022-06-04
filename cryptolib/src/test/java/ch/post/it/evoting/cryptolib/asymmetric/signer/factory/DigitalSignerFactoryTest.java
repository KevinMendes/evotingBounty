/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.ConfigDigitalSignerAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicy;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class DigitalSignerFactoryTest {

	@Test
	void whenCreateDigitalSignerFactoryUsingSha256WithRsaAndPssAndBc() {
		final DigitalSignerFactory DigitalSignerFactoryByPolicy = new DigitalSignerFactory(getDigitalSignerPolicySha256WithRsaAndPssAndBc());

		assertNotNull(DigitalSignerFactoryByPolicy.create());
	}

	private DigitalSignerPolicy getDigitalSignerPolicySha256WithRsaAndPssAndBc() {
		return new DigitalSignerPolicy() {

			@Override
			public ConfigDigitalSignerAlgorithmAndSpec getDigitalSignerAlgorithmAndSpec() {
				return ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {

				switch (OperatingSystem.current()) {
				case WINDOWS:
					return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
				case UNIX:
					return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
				default:
					throw new CryptoLibException("OS not supported");
				}
			}
		};
	}
}
