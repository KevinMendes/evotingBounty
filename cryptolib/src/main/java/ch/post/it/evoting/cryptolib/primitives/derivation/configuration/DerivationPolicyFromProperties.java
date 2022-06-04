/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import java.util.Properties;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Implementation of the {@link DerivationPolicy} interface, which reads values from a properties input. For a key that is not set in the properties
 * input, a default value will be used. The default values are:
 * <ul>
 *     <li>primitives.kdfderivation=MGF1_SHA256_BC</li>
 *     <li>primitives.pbkdfderivation=PBKDF2_32000_SHA256_256_BC_KL128</li>
 *     <li>primitives.pbkdfderivation.securerandom.unix=NATIVE_PRNG_SUN</li>
 *     <li>primitives.pbkdfderivation.securerandom.windows=PRNG_SUN_MSCAPI</li>
 *     <li>primitives.pbkdfderivation.minpasswordlength=16</li>
 *     <li>primitives.pbkdfderivation.maxpasswordlength=1000</li>
 * </ul>
 *
 * <p>Instances of this class are immutable.
 */
public final class DerivationPolicyFromProperties implements DerivationPolicy {

	static final ConfigKDFDerivationParameters DEFAULT_KDF_DERIVATION_PARAMETERS = ConfigKDFDerivationParameters.MGF1_SHA256_BC;
	static final ConfigPBKDFDerivationParameters DEFAULT_PBKDF_DERIVATION_PARAMETERS = ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final int DEFAULT_PBKDF_MIN_PW_LENGTH = 16;
	static final int DEFAULT_PBKDF_MAX_PW_LENGTH = 1000;
	static final String PRIMITIVES_KDF_DERIVATION_KEY = "primitives.kdfderivation";
	static final String PRIMITIVES_PBKDF_DERIVATION_KEY = "primitives.pbkdfderivation";
	static final String PRIMITIVES_SECURE_RANDOM_KEY = "primitives.pbkdfderivation.securerandom";
	static final String PRIMITIVES_PBKDF_DERIVATION_MIN_PWLENGTH_KEY = "primitives.pbkdfderivation.minpasswordlength";
	static final String PRIMITIVES_PBKDF_DERIVATION_MAX_PWLENGTH_KEY = "primitives.pbkdfderivation.maxpasswordlength";

	private final ConfigKDFDerivationParameters kdfDerivationParameters;
	private final ConfigPBKDFDerivationParameters pbkdfDerivationParameters;
	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
	private final int pbkdfDerivationMinPasswordLength;
	private final int pbkdfDerivationMaxPasswordLength;

	public DerivationPolicyFromProperties() {
		this(PolicyFromPropertiesHelper.loadCryptolibProperties());
	}

	/**
	 * Instantiates a property provider that reads properties from the specified path.
	 *
	 * @param properties The properties that define the policy.
	 * @throws CryptoLibException if the path is blank or incorrect.
	 */
	public DerivationPolicyFromProperties(final Properties properties) {

		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper(properties);
		try {

			kdfDerivationParameters = ConfigKDFDerivationParameters
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(PRIMITIVES_KDF_DERIVATION_KEY, DEFAULT_KDF_DERIVATION_PARAMETERS.name()));

			pbkdfDerivationParameters = ConfigPBKDFDerivationParameters
					.valueOf(helper.getNotBlankOrDefaultPropertyValue(PRIMITIVES_PBKDF_DERIVATION_KEY, DEFAULT_PBKDF_DERIVATION_PARAMETERS.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(PRIMITIVES_SECURE_RANDOM_KEY, DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

			pbkdfDerivationMinPasswordLength = Integer.parseInt(helper.getNotBlankOrDefaultPropertyValue(PRIMITIVES_PBKDF_DERIVATION_MIN_PWLENGTH_KEY,
					String.valueOf(DEFAULT_PBKDF_MIN_PW_LENGTH)));

			pbkdfDerivationMaxPasswordLength = Integer.parseInt(helper.getNotBlankOrDefaultPropertyValue(PRIMITIVES_PBKDF_DERIVATION_MAX_PWLENGTH_KEY,
					String.valueOf(DEFAULT_PBKDF_MAX_PW_LENGTH)));
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
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

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}
}
