/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.configuration;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

/**
 * Implementation of the {@link SecureRandomPolicy} interface, which reads values from a properties input. For a key that is not set in the properties
 * input, a default value will be used. The default values are:
 * <ul>
 *     <li>primitives.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>primitives.securerandom.windows=PRNG_SUN_MSCAPI</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public final class SecureRandomPolicyFromProperties implements SecureRandomPolicy {

	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;

	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;

	public SecureRandomPolicyFromProperties() {
		this(PolicyFromPropertiesHelper.loadCryptolibProperties());
	}

	/**
	 * Instantiates a new {@code SecureRandomPolicyFromProperties}.
	 *
	 * @param properties The properties that define the policy.
	 * @throws CryptoLibException if the path of properties file or properties names are invalid.
	 */
	public SecureRandomPolicyFromProperties(final Properties properties) {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper(properties);
		try {
			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue("primitives.securerandom", DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {

		return secureRandomAlgorithmAndProvider;
	}
}
