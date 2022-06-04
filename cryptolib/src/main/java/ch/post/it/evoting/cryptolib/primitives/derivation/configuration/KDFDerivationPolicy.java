/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

/**
 * Interface which specifies the methods that should be implemented by a specific {@link KDFDerivationPolicy}.
 */
public interface KDFDerivationPolicy {

	/**
	 * Returns the key derivation function parameters.
	 *
	 * @return the key derivation function parameters.
	 */
	ConfigKDFDerivationParameters getKDFDerivationParameters();
}
