/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SecretKeyPolicy;

/**
 * Implementation of the {@link AsymmetricCipherPolicy} interface, which reads values from a properties input. For a key that is not set in the
 * properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>asymmetric.cipher=RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC</li>
 *     <li>asymmetric.cipher.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>asymmetric.cipher.securerandom.windows=PRNG_SUN_MSCAPI</li>
 *     <li>asymmetric.cipher.symmetric.encryptionsecretkey=AES_128_SUNJCE</li>
 *     <li>asymmetric.cipher.symmetric.cipher=AES_WITH_GCM_AND_NOPADDING_96_128_BC</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class AsymmetricCipherPolicyFromProperties implements AsymmetricCipherPolicy, SecureRandomPolicy, SecretKeyPolicy, SymmetricCipherPolicy {

	static final ConfigAsymmetricCipherAlgorithmAndSpec DEFAULT_ASYMMETRIC_CIPHER = ConfigAsymmetricCipherAlgorithmAndSpec.RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final ConfigSecretKeyAlgorithmAndSpec DEFAULT_SECRET_KEY = ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
	static final ConfigSymmetricCipherAlgorithmAndSpec DEFAULT_SYMMETRIC_CIPHER = ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
	static final String ASYMMETRIC_CIPHER_KEY = "asymmetric.cipher";
	static final String ASYMMETRIC_CIPHER_SECURE_RANDOM_KEY = "asymmetric.cipher.securerandom";
	static final String ASYMMETRIC_CIPHER_SECURE_RANDOM_ENC_SECRETKEY_KEY = "asymmetric.cipher.symmetric.encryptionsecretkey";
	static final String ASYMMETRIC_CIPHER_SYMMETRIC_CIPHER_KEY = "asymmetric.cipher.symmetric.cipher";

	private final ConfigAsymmetricCipherAlgorithmAndSpec asymmetricCipherAlgorithmAndSpec;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
	private final ConfigSecretKeyAlgorithmAndSpec secretKeyAlgorithmAndSpec;
	private final ConfigSymmetricCipherAlgorithmAndSpec symmetricCipherAlgorithmAndSpec;

	/**
	 * Creates an asymmetric cipher policy using the provided properties.
	 *
	 * @throws CryptoLibException if {@code helper} or properties file is invalid.
	 */
	public AsymmetricCipherPolicyFromProperties() {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			asymmetricCipherAlgorithmAndSpec = ConfigAsymmetricCipherAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_CIPHER_KEY, DEFAULT_ASYMMETRIC_CIPHER.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(ASYMMETRIC_CIPHER_SECURE_RANDOM_KEY, DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

			secretKeyAlgorithmAndSpec = ConfigSecretKeyAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_CIPHER_SECURE_RANDOM_ENC_SECRETKEY_KEY, DEFAULT_SECRET_KEY.name()));

			symmetricCipherAlgorithmAndSpec = ConfigSymmetricCipherAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_CIPHER_SYMMETRIC_CIPHER_KEY, DEFAULT_SYMMETRIC_CIPHER.name()));
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigAsymmetricCipherAlgorithmAndSpec getAsymmetricCipherAlgorithmAndSpec() {

		return asymmetricCipherAlgorithmAndSpec;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {

		return secureRandomAlgorithmAndProvider;
	}

	@Override
	public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {

		return secretKeyAlgorithmAndSpec;
	}

	@Override
	public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {

		return symmetricCipherAlgorithmAndSpec;
	}
}
