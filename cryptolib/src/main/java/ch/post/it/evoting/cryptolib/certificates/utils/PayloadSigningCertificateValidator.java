/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * A service that ensures that a payload signature certificate is adequate.
 */
public interface PayloadSigningCertificateValidator {
	/**
	 * Ascertains whether the certificate chain can be traced back to the trusted certificate.
	 *
	 * @param certificateChain   the certificate chain to test
	 * @param trustedCertificate the certificate to test the certificate chain against
	 * @return whether the certificate chain is valid
	 * @throws CertificateChainValidationException if the validation could not be performed
	 */
	boolean isValid(X509Certificate[] certificateChain, X509Certificate trustedCertificate) throws CertificateChainValidationException;

	/**
	 * Returns the eventual validation errors.
	 *
	 * @return empty collection if the certificate chain is valid, the validation errors otherwise
	 */
	Collection<String> getErrors();
}
