/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting an asymmetric cipher.
 *
 * <p>These parameters are:
 *
 * <ol>
 *   <li>An algorithm/mode/padding.
 *   <li>A provider.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigAsymmetricCipherAlgorithmAndSpec {
	RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_BC("RSA/RSA-KEMWITHKDF1ANDSHA-256/NOPADDING", Provider.BOUNCY_CASTLE),

	RSA_WITH_RSA_KEM_AND_KDF1_AND_SHA256_DEFAULT("RSA/RSA-KEMWITHKDF1ANDSHA-256/NOPADDING", Provider.DEFAULT),

	RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_BC("RSA/RSA-KEMWITHKDF2ANDSHA-256/NOPADDING", Provider.BOUNCY_CASTLE),

	RSA_WITH_RSA_KEM_AND_KDF2_AND_SHA256_DEFAULT("RSA/RSA-KEMWITHKDF2ANDSHA-256/NOPADDING", Provider.DEFAULT);

	private final String algorithmModePadding;

	private final Provider provider;

	ConfigAsymmetricCipherAlgorithmAndSpec(final String algorithm, final Provider provider) {
		algorithmModePadding = algorithm;
		this.provider = provider;
	}

	public String getAlgorithmModePadding() {
		return algorithmModePadding;
	}

	public Provider getProvider() {
		return provider;
	}
}
