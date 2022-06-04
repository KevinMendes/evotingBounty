/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class PasswordDerivationPolicyFromPropertiesTest {

	private static DerivationPolicyFromProperties passwordDerivationPolicyFromFile;

	@BeforeAll
	public static void init() {
		passwordDerivationPolicyFromFile = new DerivationPolicyFromProperties();
	}

	@Test
	void whenGetPasswordDerivationPolicyFromUnix() {

		Assumptions.assumeTrue(OperatingSystem.UNIX.isCurrent());

		Assertions.assertEquals(passwordDerivationPolicyFromFile.getPBKDFDerivationParameters(),
				getPasswordDerivationPolicyForUnix().getPBKDFDerivationParameters());

		Assertions.assertEquals(passwordDerivationPolicyFromFile.getSecureRandomAlgorithmAndProvider(),
				getPasswordDerivationPolicyForUnix().getSecureRandomAlgorithmAndProvider());
	}

	@Test
	void whenGetPasswordDerivationPolicyFromWindows() {

		Assumptions.assumeTrue(OperatingSystem.WINDOWS.isCurrent());

		Assertions.assertEquals(getPasswordDerivationPolicyForWindows().getPBKDFDerivationParameters(),
				passwordDerivationPolicyFromFile.getPBKDFDerivationParameters());

		Assertions.assertEquals(getPasswordDerivationPolicyForWindows().getSecureRandomAlgorithmAndProvider(),
				passwordDerivationPolicyFromFile.getSecureRandomAlgorithmAndProvider());
	}

	private DerivationPolicy getPasswordDerivationPolicyForUnix() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_DEFAULT;
			}

			@Override
			public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
				return ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128;
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

	private DerivationPolicy getPasswordDerivationPolicyForWindows() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_DEFAULT;
			}

			@Override
			public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
				return ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128;
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
}
