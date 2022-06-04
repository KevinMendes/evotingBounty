/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.commons;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateChainValidator;

public class ChainValidator {

	public static void validateChain(Certificate trustedCertificate, Certificate[] chain, Certificate subjectCertificate, X509CertificateType type)
			throws GeneralCryptoLibException {
		// Trusted certificate
		// Get X509 certificate
		X509Certificate rootCA = (X509Certificate) trustedCertificate;

		// Get X509 certificate
		X509Certificate signerCertificate = (X509Certificate) subjectCertificate;
		X509DistinguishedName subjectDnTenantCA = getDistinguishName(signerCertificate);

		X509Certificate[] certificateChain = {};
		X509DistinguishedName[] subjectDns = {};

		if (chain.length > 0) {
			// Root vs Root chain validation
			certificateChain = new X509Certificate[chain.length];
			subjectDns = new X509DistinguishedName[chain.length];

			for (int i = 0; i < chain.length; i++) {
				certificateChain[i] = (X509Certificate) chain[i];
				subjectDns[i] = getDistinguishName(certificateChain[i]);
			}
		}

		// Validate chain
		validateCert(signerCertificate, subjectDnTenantCA, type, certificateChain, subjectDns, rootCA);
	}

	public static void validateChain(Certificate rootCertificate, Certificate subjectCertificate, X509CertificateType type)
			throws GeneralCryptoLibException {
		Certificate[] nullChain = new Certificate[] {};
		validateChain(rootCertificate, nullChain, subjectCertificate, type);
	}

	private static X509DistinguishedName getDistinguishName(X509Certificate x509Cert) throws GeneralCryptoLibException {
		CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate(x509Cert);
		return wrappedCertificate.getSubjectDn();
	}

	private static void validateCert(X509Certificate certLeaf, X509DistinguishedName subjectDnsLeaf, X509CertificateType certType,
			X509Certificate[] certChain, X509DistinguishedName[] subjectDns, X509Certificate certTrusted) throws GeneralCryptoLibException {
		X509CertificateChainValidator certificateChainValidator = createCertificateChainValidator(certLeaf, subjectDnsLeaf, certType, certChain,
				subjectDns, certTrusted);
		List<String> failedValidations = certificateChainValidator.validate();
		if (!(failedValidations == null || failedValidations.isEmpty())) {
			throw new GeneralCryptoLibException("Failed validation of chain:\n" + String.join("\n", failedValidations));
		}
	}

	private static X509CertificateChainValidator createCertificateChainValidator(X509Certificate certLeaf, X509DistinguishedName subjectDnsLeaf,
			X509CertificateType certType, X509Certificate[] certChain, X509DistinguishedName[] subjectDns, X509Certificate certTrusted)
			throws GeneralCryptoLibException {

		return new X509CertificateChainValidator(certLeaf, certType, subjectDnsLeaf, certChain, subjectDns, certTrusted);
	}
}
