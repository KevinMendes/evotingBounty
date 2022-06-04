/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.ejb.Local;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;

@Local
public interface CertificateValidationService {

	/**
	 * Validate a certificate against its parent.
	 * <p>
	 * This validation process involves the follow:
	 * <ul>
	 * <li>Validate that the certificate is not a CA.</li>
	 * <li>Validate the certificate chain.</li>
	 * <li>Validate the certificate again its parent.</li>
	 * </ul>
	 *
	 * @param certificateToValidate the certificate to validate.
	 * @param parentCertificate     the parent certificate of the certificate to validate.
	 * @return the result of the validations.
	 */
	CertificateValidationResult validateCertificate(Certificate certificateToValidate, Certificate parentCertificate)
			throws GeneralCryptoLibException, CryptographicOperationException;

	/**
	 * Validate a root certificate.
	 *
	 * @param certificateToValidate the root certificate to validate.
	 * @return the result of the validations.
	 */
	X509CertificateValidationResult validateRootCertificate(Certificate certificateToValidate) throws CryptographicOperationException;

	/**
	 * Validate a intermediate CA certificate.
	 *
	 * @param certificateToValidate the root certificate to validate.
	 * @return the result of the validations.
	 * @throws GeneralCryptoLibException
	 */
	X509CertificateValidationResult validateIntermediateCACertificate(Certificate certificateToValidate, Certificate parentCertificate)
			throws CryptographicOperationException, GeneralCryptoLibException;

	/**
	 * Validates a chain using of certificates using the cryptolib , which return the list of errors found in the process. The list will be empty if
	 * the validation is successful
	 *
	 * @param leafCert                   - leaf certificate of the chain
	 * @param leafCertSubjectDn          - Distinguished subject name of the leaf certificate
	 * @param leafCertType               - Certificate type to be validated
	 * @param intermediateCertChain      - Intermediate chain without leaf and root certificate
	 * @param intermediateCertSubjectDns - Distinguished subject names of the intermediate chain
	 * @param rootCert                   - Root certificate of the chain
	 * @return - A boolean value evaluating if the list of errors is empty
	 * @throws GeneralCryptoLibException
	 */
	boolean validateCertificateChain(X509Certificate leafCert, X509DistinguishedName leafCertSubjectDn, X509CertificateType leafCertType,
			X509Certificate[] intermediateCertChain, X509DistinguishedName[] intermediateCertSubjectDns, X509Certificate rootCert)
			throws GeneralCryptoLibException;

}
