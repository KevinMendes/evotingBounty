/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * An implementation of {@link SymmetricKeyPolicy} which is read from a properties file. For a key that is not set in the properties input, a default
 * value will be used. The default values are:
 * <ul>
 *     <li>symmetric.encryptionsecretkey=AES_128_SUNJCE</li>
 *     <li>symmetric.macsecretkey=HMAC_WITH_SHA256_256</li>
 *     <li>symmetric.key.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>symmetric.key.securerandom.windows=PRNG_SUN_MSCAPI</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public final class SymmetricKeyPolicyFromProperties implements SymmetricKeyPolicy {

	static final ConfigSecretKeyAlgorithmAndSpec DEFAULT_SECRETKEY = ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
	static final ConfigHmacSecretKeyAlgorithmAndSpec DEFAULT_HMAC_SECRETKEY = ConfigHmacSecretKeyAlgorithmAndSpec.HMAC_WITH_SHA256_256;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;

	private final ConfigSecretKeyAlgorithmAndSpec secretKeyAlgorithmAndSpec;
	private final ConfigHmacSecretKeyAlgorithmAndSpec hmacSecretKeyAlgorithmAndSpec;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;

	/**
	 * Creates a symmetric key policy using properties which are read from the properties file at the specified path.
	 *
	 * @throws CryptoLibException if helper is invalid or properties are invalid.
	 */
	public SymmetricKeyPolicyFromProperties() {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			secretKeyAlgorithmAndSpec = ConfigSecretKeyAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue("symmetric.encryptionsecretkey", DEFAULT_SECRETKEY.name()));

			hmacSecretKeyAlgorithmAndSpec = ConfigHmacSecretKeyAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue("symmetric.macsecretkey", DEFAULT_HMAC_SECRETKEY.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue("symmetric.key.securerandom", DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

			if (!secureRandomAlgorithmAndProvider.isOSCompliant(OperatingSystem.current())) {
				throw new CryptoLibException("Illegal property value");
			}

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
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
	public ConfigHmacSecretKeyAlgorithmAndSpec getHmacSecretKeyAlgorithmAndSpec() {
		return hmacSecretKeyAlgorithmAndSpec;
	}
}
