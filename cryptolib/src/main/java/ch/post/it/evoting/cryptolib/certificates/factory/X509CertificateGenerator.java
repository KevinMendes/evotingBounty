/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;

/**
 * Generates a x509 certificate.
 */
public class X509CertificateGenerator {
	private final CertificatesServiceAPI certificatesService;

	private final CertificateDataBuilder certificateDataBuilder;

	public X509CertificateGenerator(final CertificatesServiceAPI certificatesService, final CertificateDataBuilder certificateDataBuilder) {
		this.certificatesService = certificatesService;
		this.certificateDataBuilder = certificateDataBuilder;

	}

	public CryptoAPIX509Certificate generate(final CertificateParameters certificateParameters, final PublicKey subjectPublicKey,
			final PrivateKey caPrivateKey) throws GeneralCryptoLibException {
		final CertificateData certificateData = certificateDataBuilder.build(subjectPublicKey, certificateParameters);

		CryptoAPIX509Certificate certificate = null;

		switch (certificateParameters.getType()) {
		case ROOT:
			certificate = certificatesService.createRootAuthorityX509Certificate(certificateData, caPrivateKey);
			break;
		case INTERMEDIATE:
			certificate = certificatesService.createIntermediateAuthorityX509Certificate(certificateData, caPrivateKey);
			break;
		case SIGN:
			certificate = certificatesService.createSignX509Certificate(certificateData, caPrivateKey);
			break;
		case ENCRYPTION:
			certificate = certificatesService.createEncryptionX509Certificate(certificateData, caPrivateKey);
			break;
		default:
			break;

		}

		return certificate;
	}
}
