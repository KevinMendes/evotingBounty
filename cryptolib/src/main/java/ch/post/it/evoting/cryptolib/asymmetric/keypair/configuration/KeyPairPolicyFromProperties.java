/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.factory.KeyPairGeneratorFactory;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * An implementation of the {@link KeyPairPolicy} interface, which reads values from a properties input. For a key that is not set in the properties
 * input, a default value will be used. The default values are:
 * <ul>
 *     <li>asymmetric.signingkeypair=RSA_2048_F4_SUN_RSA_SIGN</li>
 *     <li>asymmetric.encryptionkeypair=RSA_2048_F4_SUN_RSA_SIGN</li>
 *     <li>asymmetric.keypair.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>asymmetric.keypair.securerandom.windows=PRNG_SUN_MSCAPI</li>
 * </ul>
 *
 * <p>Instances of this policy fully configure the behavior of {@link
 * KeyPairGeneratorFactory}.
 *
 * <p>Instances of this class are immutable.
 */
public final class KeyPairPolicyFromProperties implements KeyPairPolicy {

	static final ConfigSigningKeyPairAlgorithmAndSpec DEFAULT_SIGNING_KEYPAIR = ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
	static final ConfigEncryptionKeyPairAlgorithmAndSpec DEFAULT_ENCRYPTION_KEYPAIR = ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final String ASYMMETRIC_SIGNING_KEYPAIR_KEY = "asymmetric.signingkeypair";
	static final String ASYMMETRIC_ENCRYPTION_KEYPAIR_KEY = "asymmetric.encryptionkeypair";
	static final String ASYMMETRIC_KEYPAIR_SECURE_RANDOM_KEY = "asymmetric.keypair.securerandom";

	private final ConfigSigningKeyPairAlgorithmAndSpec signingAlgorithm;
	private final ConfigEncryptionKeyPairAlgorithmAndSpec encryptionAlgorithm;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;

	/**
	 * Constructs an instance of KeyPairPolicyFromProperties from the standard properties.
	 *
	 * <p>Note: a single property is used to configure the secure random algorithm and provider that
	 * is used when creating both signing and encryption asymmetric keys.
	 *
	 * @throws CryptoLibException if {@code helper} or properties file is invalid.
	 */
	public KeyPairPolicyFromProperties() {
		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();

		try {
			signingAlgorithm = ConfigSigningKeyPairAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_SIGNING_KEYPAIR_KEY, DEFAULT_SIGNING_KEYPAIR.name()));

			encryptionAlgorithm = ConfigEncryptionKeyPairAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_ENCRYPTION_KEYPAIR_KEY, DEFAULT_ENCRYPTION_KEYPAIR.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(ASYMMETRIC_KEYPAIR_SECURE_RANDOM_KEY, DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException(
					"An IllegalArgumentException exception was thrown while trying to obtain a secure random property from a properties source.", e);
		}
	}

	@Override
	public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
		return signingAlgorithm;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}

	@Override
	public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
		return encryptionAlgorithm;
	}
}
