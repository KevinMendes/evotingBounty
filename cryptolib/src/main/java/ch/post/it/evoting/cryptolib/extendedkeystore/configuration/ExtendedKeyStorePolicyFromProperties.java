/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigPBKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SecretKeyPolicy;

/**
 * Implementation of the {@link ExtendedKeyStorePolicy} interface, which reads values from a properties input. For a key that is not set in the
 * properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>extended.keystore.[key]=PKCS12_SUN_JSSE</li>
 *     <li>primitives.kdfderivation.[key]=MGF1_SHA256_BC</li>
 *     <li>primitives.pbkdfderivation.[key]=PBKDF2_32000_SHA256_256_BC_KL128</li>
 *     <li>primitives.pbkdfderivation.securerandom.[key].unix=NATIVE_PRNG_SUN</li>
 *     <li>primitives.pbkdfderivation.securerandom.[key].windows=PRNG_SUN_MSCAPI</li>
 *     <li>symmetric.encryptionsecretkey=AES_128_SUNJCE</li>
 *     <li>symmetric.cipher=AES_WITH_GCM_AND_NOPADDING_96_128_BC</li>
 *     <li>primitives.pbkdfderivation.minpasswordlength=16</li>
 *     <li>primitives.pbkdfderivation.maxpasswordlength=1000</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class ExtendedKeyStorePolicyFromProperties implements ExtendedKeyStorePolicy, SecretKeyPolicy {

	static final ConfigExtendedKeyStoreTypeAndProvider DEFAULT_KEYSTORE_TYPE_PROVIDER = ConfigExtendedKeyStoreTypeAndProvider.PKCS12_SUN_JSSE;
	static final ConfigKDFDerivationParameters DEFAULT_KDF_DERIVATION = ConfigKDFDerivationParameters.MGF1_SHA256_BC;
	static final ConfigPBKDFDerivationParameters DEFAULT_PBKDF_DERIVATION = ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final ConfigSecretKeyAlgorithmAndSpec DEFAULT_SECRET_KEY_ALGORITHM = ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
	static final ConfigSymmetricCipherAlgorithmAndSpec DEFAULT_CIPHER_ALGORITHM = ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
	static final int DEFAULT_PBKDF_MIN_PW_LENGTH = 16;
	static final int DEFAULT_PBKDF_MAX_PW_LENGTH = 1000;

	private final ConfigExtendedKeyStoreTypeAndProvider storeTypeAndProvider;
	private final ConfigKDFDerivationParameters kdfDerivationParameters;
	private final ConfigPBKDFDerivationParameters pbkdfDerivationParameters;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
	private final ConfigSecretKeyAlgorithmAndSpec secretKeyAlgorithmAndSpec;
	private final ConfigSymmetricCipherAlgorithmAndSpec symmetricCipherAlgorithmAndSpec;
	private final int pbkdfDerivationMinPasswordLength;
	private final int pbkdfDerivationMaxPasswordLength;

	/**
	 * Creates an instance of the class and initializes it by parameter from provided path.
	 *
	 * @param key the key used to retrieve properties from the properties input.
	 * @throws CryptoLibException if {@code helper} or {@code key} is invalid.
	 */
	public ExtendedKeyStorePolicyFromProperties(final String key) {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		final String trimmedKey = key.trim();

		try {
			Key.valueOf(trimmedKey.toUpperCase());

			storeTypeAndProvider = ConfigExtendedKeyStoreTypeAndProvider.valueOf(
					helper.getNotBlankOrDefaultPropertyValue(String.format("extended.keystore.%s", trimmedKey),
							DEFAULT_KEYSTORE_TYPE_PROVIDER.name()));

			kdfDerivationParameters = ConfigKDFDerivationParameters.valueOf(
					helper.getNotBlankOrDefaultPropertyValue(String.format("primitives.kdfderivation.%s", trimmedKey),
							DEFAULT_KDF_DERIVATION.name()));

			pbkdfDerivationParameters = ConfigPBKDFDerivationParameters.valueOf(
					helper.getNotBlankOrDefaultPropertyValue(String.format("primitives.pbkdfderivation.%s", trimmedKey),
							DEFAULT_PBKDF_DERIVATION.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(String.format("primitives.pbkdfderivation.securerandom.%s", trimmedKey),
							DEFAULT_UNIX_SECURE_RANDOM.name(), DEFAULT_WINDOWS_SECURE_RANDOM.name()));

			if (!secureRandomAlgorithmAndProvider.isOSCompliant(OperatingSystem.current())) {
				throw new CryptoLibException("Illegal property value");
			}

			secretKeyAlgorithmAndSpec = ConfigSecretKeyAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue("symmetric.encryptionsecretkey", DEFAULT_SECRET_KEY_ALGORITHM.name()));

			symmetricCipherAlgorithmAndSpec = ConfigSymmetricCipherAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue("symmetric.cipher", DEFAULT_CIPHER_ALGORITHM.name()));
			pbkdfDerivationMinPasswordLength = Integer.parseInt(
					helper.getNotBlankOrDefaultPropertyValue("primitives.pbkdfderivation.minpasswordlength",
							String.valueOf(DEFAULT_PBKDF_MIN_PW_LENGTH)));

			pbkdfDerivationMaxPasswordLength = Integer.parseInt(
					helper.getNotBlankOrDefaultPropertyValue("primitives.pbkdfderivation.maxpasswordlength",
							String.valueOf(DEFAULT_PBKDF_MAX_PW_LENGTH)));
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigExtendedKeyStoreTypeAndProvider getStoreTypeAndProvider() {
		return storeTypeAndProvider;
	}

	@Override
	public ConfigKDFDerivationParameters getKDFDerivationParameters() {
		return kdfDerivationParameters;
	}

	@Override
	public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
		return pbkdfDerivationParameters;
	}

	@Override
	public int getPBKDFDerivationMinPasswordLength() {
		return pbkdfDerivationMinPasswordLength;
	}

	@Override
	public int getPBKDFDerivationMaxPasswordLength() {
		return pbkdfDerivationMaxPasswordLength;
	}

	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}

	public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
		return secretKeyAlgorithmAndSpec;
	}

	public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {
		return symmetricCipherAlgorithmAndSpec;
	}

	protected enum Key {
		P12
	}
}
