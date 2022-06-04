/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;

public class CryptolibPayloadSigningCertificateValidator implements PayloadSigningCertificateValidator {

	private List<String> errors = new ArrayList<>();

	/**
	 * Extract an X509 distinguished name from an X509 certificate which, contrary to what the class names might suggest, is not trivial.
	 *
	 * @param certificate the X509 certificate
	 * @return the X509-compliant distinguished name
	 */
	private static X509DistinguishedName getDNFromX509Certificate(final X509Certificate certificate) {
		try {
			return getCryptoX509Certificate(certificate).getSubjectDn();
		} catch (final CryptographicOperationException e) {
			throw new IllegalArgumentException("The certificate could not be converted", e);
		}
	}

	public static CryptoX509Certificate getCryptoX509Certificate(final Certificate certificateToValidate) throws CryptographicOperationException {

		final X509Certificate x509Certificate = (X509Certificate) certificateToValidate;

		final CryptoX509Certificate cryptoX509Certificate;
		try {
			cryptoX509Certificate = new CryptoX509Certificate(x509Certificate);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured while trying to create a CryptoX509Certificate from the received certificate",
					e);
		}

		return cryptoX509Certificate;
	}

	@Override
	public boolean isValid(final X509Certificate[] certificateChain, final X509Certificate trustedCertificate) throws CertificateChainValidationException {
		// Shortcut for obviously wrong certificate chains.
		if (certificateChain.length == 0) {
			errors.add("Empty certificate chain");
			return false;
		}

		// Measure the intermediate certificate chain (i.e. minus the leaf
		// certificate).
		final int intermediateCertificateChainLength = certificateChain.length - 1;
		// Build the intermediate certificate chain.
		final X509Certificate[] intermediateCertificateChain = new X509Certificate[intermediateCertificateChainLength];
		System.arraycopy(certificateChain, 1, intermediateCertificateChain, 0, intermediateCertificateChainLength);
		// Get the intermediate certificate subject DNs.
		final X509DistinguishedName[] intermediateCertificateSubjectDNs = Stream.of(intermediateCertificateChain)
				.map(CryptolibPayloadSigningCertificateValidator::getDNFromX509Certificate).toArray(X509DistinguishedName[]::new);
		// Verify the certificate chain against the root certificate.
		final X509Certificate leafCertificate = certificateChain[0];

		final X509CertificateChainValidator certificateChainValidator;
		try {
			certificateChainValidator = new X509CertificateChainValidator(leafCertificate, X509CertificateType.SIGN,
					getDNFromX509Certificate(leafCertificate), intermediateCertificateChain, intermediateCertificateSubjectDNs, trustedCertificate);
			errors = certificateChainValidator.validate();

			return errors.isEmpty();
		} catch (final GeneralCryptoLibException e) {
			throw new CertificateChainValidationException(e);
		}
	}

	@Override
	public Collection<String> getErrors() {
		return errors;
	}
}
