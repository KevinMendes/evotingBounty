/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.security.Security;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigPBKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

class CryptoPBKDFDeriverTest {

	private static final double SUN_JCE_VERSION = 1.8d;
	private static final int SALT_BYTES_LENGTH = 1024;
	private static final PrimitivesServiceAPI primitivesService = new PrimitivesService();

	private final CryptoKeyDeriverFactory _cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(getKeyDerivationPolicy());

	@Test
	void testThatDerivesKeyForPBKDF2SHA256BCProviderKL128() throws GeneralCryptoLibException {
		final char[] password = "passwordTESTpassword".toCharArray();

		final CryptoPBKDFDeriver cryptoPBKDFDeriver = _cryptoKeyDeriverFactory.createPBKDFDeriver();
		final CryptoAPIDerivedKey cryptoPBKDFDerivedKey = cryptoPBKDFDeriver.deriveKey(password, generateRandomSalt());

		assertNotNull(cryptoPBKDFDerivedKey);
	}

	@Test
	void testThatDerivesPasswordPBKDF2SHA256BCProviderKL128() throws GeneralCryptoLibException {
		final char[] password = "passwordTESTpassword".toCharArray();

		final CryptoAPIPBKDFDeriver deriver = _cryptoKeyDeriverFactory.createPBKDFDeriver();
		final CryptoAPIDerivedKey deriveKey = deriver.deriveKey(password, generateRandomSalt());

		assertNotNull(deriveKey);
	}

	@Test
	void testThatDerivesPasswordPBKDF2SHA256SunJCEProviderKL128() throws GeneralCryptoLibException {
		final double version = Security.getProvider(Provider.SUN_JCE.getProviderName()).getVersion();
		assumeTrue(version >= SUN_JCE_VERSION);

		final char[] password = "passwordTESTpassword".toCharArray();

		final CryptoKeyDeriverFactory cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(getKeyDerivationSHA256Policy());
		final CryptoAPIPBKDFDeriver deriver = cryptoKeyDeriverFactory.createPBKDFDeriver();
		final CryptoAPIDerivedKey deriveKey = deriver.deriveKey(password, generateRandomSalt());

		assertNotNull(deriveKey);
	}

	@Test
	void testThatDerivesShortKeyForPBKDF2SHA256BCProviderKL128() {
		final char[] password = "short".toCharArray();

		final CryptoPBKDFDeriver cryptoPBKDFDeriver = _cryptoKeyDeriverFactory.createPBKDFDeriver();

		assertThrows(GeneralCryptoLibException.class, () -> cryptoPBKDFDeriver.deriveKey(password, generateRandomSalt()));
	}

	@Test
	void testThatDerivesShortPasswordPBKDF2SHA256BCProviderKL128() {
		final char[] password = "short".toCharArray();

		final CryptoPBKDFDeriver cryptoDerivedFromPassword = _cryptoKeyDeriverFactory.createPBKDFDeriver();

		assertThrows(GeneralCryptoLibException.class, () -> cryptoDerivedFromPassword.deriveKey(password, generateRandomSalt()));
	}

	@Test
	void testThatDerivesLongKeyForPBKDF2SHA256BCProviderKL128() {
		final char[] password = new char[1024];
		Arrays.fill(password, 'o');

		final CryptoPBKDFDeriver cryptoPBKDFDeriver = _cryptoKeyDeriverFactory.createPBKDFDeriver();

		assertThrows(GeneralCryptoLibException.class, () -> cryptoPBKDFDeriver.deriveKey(password, generateRandomSalt()));
	}

	@Test
	void testThatDerivesLongPasswordPBKDF2SHA256BCProviderKL128() {
		final char[] password = new char[1024];
		Arrays.fill(password, 'o');

		final CryptoPBKDFDeriver cryptoDerivedFromPassword = _cryptoKeyDeriverFactory.createPBKDFDeriver();

		assertThrows(GeneralCryptoLibException.class, () -> cryptoDerivedFromPassword.deriveKey(password, generateRandomSalt()));
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

	private DerivationPolicy getKeyDerivationSHA256Policy() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_BC;
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
				if (OperatingSystem.WINDOWS.isCurrent()) {
					return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
				}
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}
		};
	}

	private DerivationPolicy getKeyDerivationPolicy() {
		if (OperatingSystem.WINDOWS.isCurrent()) {
			return getKeyDerivationPolicyForWindows();
		}
		return getKeyDerivationPolicyForUnix();
	}

	private byte[] generateRandomSalt() {
		try {
			return primitivesService.genRandomBytes(SALT_BYTES_LENGTH);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}
}
