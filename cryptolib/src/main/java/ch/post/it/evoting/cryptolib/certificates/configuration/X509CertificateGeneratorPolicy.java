/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * Interface to retrieve the X509 certificate generation cryptographic policy settings.
 */
public interface X509CertificateGeneratorPolicy extends SecureRandomPolicy {

	/**
	 * Retrieves the algorithm used to sign the certificate with the issuer private key and the cryptographic service provider of the certificate.
	 *
	 * @return signature algorithm and provider of certificate.
	 */
	ConfigX509CertificateAlgorithmAndProvider getCertificateAlgorithmAndProvider();
}
