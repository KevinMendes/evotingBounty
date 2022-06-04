/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import java.security.Principal;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificatePublicData;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.BouncyCastleX509CertificateBuilder;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.X509CertificateBuilder;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;

/**
 * Class to generate an X509 certificate.
 */
public class CryptoX509CertificateGenerator {

	private final String signatureAlgorithm;

	private final Provider provider;

	private final CryptoRandomInteger cryptoIntegerRandom;

	/**
	 * Creates a certificate generator with the provided arguments.
	 *
	 * @param signatureAlgorithm  algorithm used to sign certificates.
	 * @param provider            name of cryptographic service {@link Provider} of certificates.
	 * @param cryptoIntegerRandom generator of random integers.
	 */
	CryptoX509CertificateGenerator(final String signatureAlgorithm, final Provider provider, final CryptoRandomInteger cryptoIntegerRandom) {

		this.signatureAlgorithm = signatureAlgorithm;
		this.provider = provider;
		this.cryptoIntegerRandom = cryptoIntegerRandom;
	}

	/**
	 * Builds the certificate.
	 *
	 * @param certificateGenerationParameters the parameters required to create the certificate.
	 * @param extensions                      the certificate extension.
	 * @param issuerPrivateKey                private key of issuer of certificate.
	 * @return a {@link CryptoX509Certificate} certificate.
	 * @throws GeneralCryptoLibException if {@code certificateGenerationParameters} is invalid or {@code issuerPrivateKey} cannot be parsed.
	 */
	public CryptoAPIX509Certificate generate(final CertificatePublicData certificateGenerationParameters,
			final AbstractCertificateExtension[] extensions, final PrivateKey issuerPrivateKey) throws GeneralCryptoLibException {

		Validate.notNull(certificateGenerationParameters, "Certificate generation parameters");
		Validate.notNullAndNoNulls(extensions, "Certificate extensions");
		Validate.notNull(issuerPrivateKey, "Issuer private key");

		final X509CertificateBuilder x509CertBuilder = createIX509CertificateBuilder(certificateGenerationParameters);

		// Add certificate extensions to builder. Convert to Set to remove duplicates if any.
		final Set<AbstractCertificateExtension> extensionSet = new HashSet<>(Arrays.asList(extensions));
		for (final AbstractCertificateExtension extension : extensionSet) {
			x509CertBuilder.addExtension(extension);
		}

		return new CryptoX509Certificate(x509CertBuilder.build(issuerPrivateKey));
	}

	private X509CertificateBuilder createIX509CertificateBuilder(final CertificatePublicData certificateGenerationParameters) {
		final Principal issuerDn;

		if (certificateGenerationParameters.getIssuerDnPrincipal() != null) {
			issuerDn = certificateGenerationParameters.getIssuerDnPrincipal();
		} else {
			issuerDn = BouncyCastleX509CertificateBuilder.getPrincipalIssuerDn(certificateGenerationParameters.getIssuerDn());
		}
		return new BouncyCastleX509CertificateBuilder(signatureAlgorithm, provider, certificateGenerationParameters.getSubjectPublicKey(),
				certificateGenerationParameters.getSubjectDn(), issuerDn, certificateGenerationParameters.getValidityDates(), cryptoIntegerRandom);
	}

}
