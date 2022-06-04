/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting a message digest generator.
 *
 * <p>These parameters are:
 *
 * <ol>
 *   <li>An algorithm.
 *   <li>A provider.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigMessageDigestAlgorithmAndProvider {
	SHA256_SUN(HashAlgorithm.SHA256, Provider.SUN),

	SHA256_BC(HashAlgorithm.SHA256, Provider.BOUNCY_CASTLE),

	SHA256_DEFAULT(HashAlgorithm.SHA256, Provider.DEFAULT),

	SHA512_224_BC(HashAlgorithm.SHA512_224, Provider.BOUNCY_CASTLE),

	SHA512_224_DEFAULT(HashAlgorithm.SHA512_224, Provider.DEFAULT),

	SHA3_256_BC(HashAlgorithm.SHA3_256, Provider.BOUNCY_CASTLE),

	SHA3_384_BC(HashAlgorithm.SHA3_384, Provider.BOUNCY_CASTLE),

	SHA3_512_BC(HashAlgorithm.SHA3_512, Provider.BOUNCY_CASTLE);

	private final HashAlgorithm algorithm;

	private final Provider provider;

	ConfigMessageDigestAlgorithmAndProvider(final HashAlgorithm algorithm, final Provider provider) {
		this.algorithm = algorithm;
		this.provider = provider;
	}

	/**
	 * Returns the configured algorithm.
	 *
	 * @return The algorithm.
	 */
	public String getAlgorithm() {
		return algorithm.getAlgorithm();
	}

	/**
	 * Returns the configured provider.
	 *
	 * @return The provider.
	 */
	public Provider getProvider() {
		return provider;
	}
}
