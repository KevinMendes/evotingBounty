/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum representing a set of parameters that can be used when requesting a MAC generator.
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
@SuppressWarnings("squid:S1192") // Ignore 'String literals should not be duplicated' Sonar's rule for this enum definition.
public enum ConfigMacAlgorithmAndProvider {
	HMAC_WITH_SHA256_SUN("HmacSHA256", Provider.SUN_JCE),

	HMAC_WITH_SHA256_BC("HmacSHA256", Provider.BOUNCY_CASTLE),

	HMAC_WITH_SHA256_DEFAULT("HmacSHA256", Provider.DEFAULT);

	private final String algorithm;

	private final Provider provider;

	ConfigMacAlgorithmAndProvider(final String algorithm, final Provider provider) {
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
