/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateGeneratorPolicy;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateGeneratorPolicyFromProperties;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;

/**
 * Factory class for creating {@link CryptoX509CertificateGenerator}.
 */
public final class X509CertificateGeneratorFactory extends CryptolibFactory {

	private final X509CertificateGeneratorPolicy x509CertGeneratorPolicy;

	/**
	 * Constructor which uses a default {@link X509CertificateGeneratorPolicy}.
	 */
	public X509CertificateGeneratorFactory() {

		this.x509CertGeneratorPolicy = new X509CertificateGeneratorPolicyFromProperties();
	}

	/**
	 * Creates a certificate generator.
	 *
	 * @return a {@link CryptoX509CertificateGenerator}.
	 */
	public CryptoX509CertificateGenerator create() {

		final String algorithm = x509CertGeneratorPolicy.getCertificateAlgorithmAndProvider().getAlgorithm();
		final Provider provider = x509CertGeneratorPolicy.getCertificateAlgorithmAndProvider().getProvider();

		final SecureRandomFactory secureRandomFactory = new SecureRandomFactory(x509CertGeneratorPolicy);

		return new CryptoX509CertificateGenerator(algorithm, provider, secureRandomFactory.createIntegerRandom());
	}
}
