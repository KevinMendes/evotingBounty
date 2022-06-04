/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

/**
 * Defines the methods that must be implemented by any secret key policy.
 */
public interface SecretKeyPolicy {

	/**
	 * Returns a {@link ConfigSecretKeyAlgorithmAndSpec} which specifies the configuration values to be used when creating a secret key.
	 *
	 * @return a configuration values.
	 */
	ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec();
}
