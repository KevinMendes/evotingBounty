/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Implementation of the {@link DigitalSignerPolicy} interface, which reads values from a properties input. For a key that is not set in the
 * properties input, a default value will be used. The default values are:
 * <ul>
 *     <li>asymmetric.signer=SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC</li>
 *     <li>asymmetric.signer.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>asymmetric.signer.securerandom.windows=PRNG_SUN_MSCAPI</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public class DigitalSignerPolicyFromProperties implements DigitalSignerPolicy {

	static final ConfigDigitalSignerAlgorithmAndSpec DEFAULT_ASYMMETRIC_SIGNER = ConfigDigitalSignerAlgorithmAndSpec.SHA256_WITH_RSA_AND_PSS_SHA256_MGF1_SHA256_32_1_BC;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final String ASYMMETRIC_SIGNER_KEY = "asymmetric.signer";
	static final String ASYMMETRIC_SIGNER_SECURE_RANDOM_KEY = "asymmetric.signer.securerandom";

	private final ConfigDigitalSignerAlgorithmAndSpec signerAlgorithmAndSpec;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;

	/**
	 * Creates a policy configured with the standard properties
	 *
	 * @throws CryptoLibException if {@code helper} or properties are invalid.
	 */
	public DigitalSignerPolicyFromProperties() {
		this(PolicyFromPropertiesHelper.loadCryptolibProperties());
	}

	/**
	 * Creates a policy configured with the given properties.
	 *
	 * @param properties the properties that define the policy.
	 * @throws CryptoLibException if {@code helper} or properties are invalid.
	 */
	public DigitalSignerPolicyFromProperties(final Properties properties) {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper(properties);
		try {
			signerAlgorithmAndSpec = ConfigDigitalSignerAlgorithmAndSpec
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(ASYMMETRIC_SIGNER_KEY, DEFAULT_ASYMMETRIC_SIGNER.name()));

			// Pick up the PRNG configuration.
			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(ASYMMETRIC_SIGNER_SECURE_RANDOM_KEY, DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigDigitalSignerAlgorithmAndSpec getDigitalSignerAlgorithmAndSpec() {

		return signerAlgorithmAndSpec;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}
}
