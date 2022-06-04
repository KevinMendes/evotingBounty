/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Implementation of the {@link SymmetricCipherPolicy} interface, which reads values from a properties input. For a key that is not set in the
 * properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>symmetric.cipher.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>symmetric.cipher.securerandom.windows=PRNG_SUN_MSCAPI</li>
 *     <li>symmetric.cipher=AES_WITH_GCM_AND_NOPADDING_96_128_BC</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class SymmetricCipherPolicyFromProperties implements SymmetricCipherPolicy {

	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final ConfigSymmetricCipherAlgorithmAndSpec DEFAULT_SYMMETRIC_CIPHER = ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;

	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
	private final ConfigSymmetricCipherAlgorithmAndSpec cipherAlgorithmAndSpec;

	/**
	 * Creates a symmetric cipher policy using properties.
	 *
	 * @throws CryptoLibException if the path is blank.
	 */
	public SymmetricCipherPolicyFromProperties() {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue("symmetric.cipher.securerandom", DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

			cipherAlgorithmAndSpec = ConfigSymmetricCipherAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue("symmetric.cipher", DEFAULT_SYMMETRIC_CIPHER.name()));
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {

		return cipherAlgorithmAndSpec;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {

		return secureRandomAlgorithmAndProvider;
	}
}
