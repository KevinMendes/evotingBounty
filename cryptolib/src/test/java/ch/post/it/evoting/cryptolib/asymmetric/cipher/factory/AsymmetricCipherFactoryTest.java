/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.AsymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.ConfigAsymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;

class AsymmetricCipherFactoryTest {

	private static ConfigSecureRandomAlgorithmAndProvider getSecureRandomConfig() {

		switch (OperatingSystem.current()) {
		case WINDOWS:
			return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
		case UNIX:
			return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
		default:
			throw new CryptoLibException("OS not supported");
		}
	}

	@Test
	void whenCreateAsymmetricCipherFactoryUsingPolicyRsaKemMgf1WithSha256AndNoPaddingAndBc() {
		final AsymmetricCipherFactory asymmetricCipherFactoryByPolicy = new AsymmetricCipherFactory(
				getAsymmetricCipherPolicyRsaKemWithKdf2AndSha256AndBc());

		assertNotNull(asymmetricCipherFactoryByPolicy.create());
	}

	private AsymmetricCipherPolicy getAsymmetricCipherPolicyRsaKemWithKdf2AndSha256AndBc() {
		return new AsymmetricCipherPolicy() {

			@Override
			public ConfigAsymmetricCipherAlgorithmAndSpec getAsymmetricCipherAlgorithmAndSpec() {
				return ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getSecureRandomConfig();
			}

			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
			}

			@Override
			public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {
				return ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
			}
		};
	}
}
