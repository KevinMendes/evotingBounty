/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum which defines the algorithm and {@link Provider} of an X509 certificate.
 */
public enum ConfigX509CertificateAlgorithmAndProvider {
	SHA256_WITH_RSA_BC("SHA256withRSA", Provider.BOUNCY_CASTLE),

	SHA256_WITH_RSA_DEFAULT("SHA256withRSA", Provider.DEFAULT);

	private final String algorithm;

	private final Provider provider;

	ConfigX509CertificateAlgorithmAndProvider(final String algorithm, final Provider provider) {
		this.algorithm = algorithm;
		this.provider = provider;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Provider getProvider() {
		return provider;
	}
}
