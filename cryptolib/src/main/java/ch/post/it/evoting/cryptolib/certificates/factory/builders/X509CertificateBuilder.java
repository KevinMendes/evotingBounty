/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;

/**
 * Interface for the X509 certificate builders.
 */
public interface X509CertificateBuilder {

	/**
	 * Builds the certificate.
	 *
	 * @param issuerPrivateKey private key of certificate issuer.
	 * @return the X509 certificate.
	 */
	X509Certificate build(final PrivateKey issuerPrivateKey);

	/**
	 * Adds a certificate extension to the certificate builder.
	 *
	 * @param extension certificate extension to add.
	 */
	void addExtension(final AbstractCertificateExtension extension);
}
