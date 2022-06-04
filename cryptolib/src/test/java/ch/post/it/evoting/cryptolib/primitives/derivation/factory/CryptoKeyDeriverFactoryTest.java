/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigPBKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class CryptoKeyDeriverFactoryTest {

	private final DerivationPolicy _derivationPolicy = getKeyDerivationPolicy();
	private final CryptoKeyDeriverFactory cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(_derivationPolicy);

	@Test
	void whenCreateCryptoKDFDeriver() {
		final CryptoKDFDeriver cryptoKDFDeriver = cryptoKeyDeriverFactory.createKDFDeriver();

		assertNotNull(cryptoKDFDeriver);
	}

	@Test
	void whenCreateCryptoPBKDFDeriver() {
		final CryptoPBKDFDeriver cryptoPBKDFDeriver = cryptoKeyDeriverFactory.createPBKDFDeriver();

		assertNotNull(cryptoPBKDFDeriver);
	}

	private DerivationPolicy getKeyDerivationPolicyForUnix() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_BC;
			}

			@Override
			public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
				return ConfigPBKDFDerivationParameters.PBKDF2_1_SHA256_256_BC_KL128;
			}

			@Override
			public int getPBKDFDerivationMinPasswordLength() {
				return DerivationConstants.MINIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public int getPBKDFDerivationMaxPasswordLength() {
				return DerivationConstants.MAXIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}
		};
	}

	private DerivationPolicy getKeyDerivationPolicyForWindows() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_BC;
			}

			@Override
			public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
				return ConfigPBKDFDerivationParameters.PBKDF2_1_SHA256_256_BC_KL128;
			}

			@Override
			public int getPBKDFDerivationMinPasswordLength() {
				return DerivationConstants.MINIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public int getPBKDFDerivationMaxPasswordLength() {
				return DerivationConstants.MAXIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			}
		};
	}

	private DerivationPolicy getKeyDerivationPolicy() {
		if (OperatingSystem.WINDOWS.isCurrent()) {
			return getKeyDerivationPolicyForWindows();
		}
		return getKeyDerivationPolicyForUnix();
	}
}
