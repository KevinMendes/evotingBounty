/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Implementation of the {@link X509CertificateGeneratorPolicy} interface, to retrieve the X509 certificate generator cryptographic policy from a
 * given properties input. For a key that is not set in the properties input, a default value will be used. The default values are:
 * <lu>
 * <li>certificates.x509certificate=SHA256_WITH_RSA_BC</li>
 * <li>certificates.x509certificate.securerandom.unix=NATIVE_PRNG_SUN</li>
 * <li>certificates.x509certificate.securerandom.windows=PRNG_SUN_MSCAPI</li>
 * </lu>
 *
 * <p>Instances of this class are immutable.
 */
public class X509CertificateGeneratorPolicyFromProperties implements X509CertificateGeneratorPolicy {

	static final ConfigX509CertificateAlgorithmAndProvider DEFAULT_CERTIFICATE_ALGORITHM_PROVIDER = ConfigX509CertificateAlgorithmAndProvider.SHA256_WITH_RSA_BC;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_UNIX_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	static final ConfigSecureRandomAlgorithmAndProvider DEFAULT_WINDOWS_SECURE_RANDOM = ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	static final String CERTIFICATE_ALGORITHM_PROVIDER_KEY = "certificates.x509certificate";
	static final String CERTIFCATE_SECURE_RANDOM = "certificates.x509certificate.securerandom";

	private final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider;
	private final ConfigX509CertificateAlgorithmAndProvider algorithmAndProvider;

	/**
	 * Creates a X509 certificate generator cryptographic policy using the standard properties
	 *
	 * @throws CryptoLibException If invalid properties were found
	 */
	public X509CertificateGeneratorPolicyFromProperties() {
		final PolicyFromPropertiesHelper helper = new PolicyFromPropertiesHelper();
		try {
			algorithmAndProvider = ConfigX509CertificateAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultPropertyValue(CERTIFICATE_ALGORITHM_PROVIDER_KEY, DEFAULT_CERTIFICATE_ALGORITHM_PROVIDER.name()));

			secureRandomAlgorithmAndProvider = ConfigSecureRandomAlgorithmAndProvider.valueOf(
					helper.getNotBlankOrDefaultOSDependentPropertyValue(CERTIFCATE_SECURE_RANDOM, DEFAULT_UNIX_SECURE_RANDOM.name(),
							DEFAULT_WINDOWS_SECURE_RANDOM.name()));

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("Illegal property value", e);
		}
	}

	@Override
	public ConfigX509CertificateAlgorithmAndProvider getCertificateAlgorithmAndProvider() {

		return algorithmAndProvider;
	}

	@Override
	public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
		return secureRandomAlgorithmAndProvider;
	}
}
