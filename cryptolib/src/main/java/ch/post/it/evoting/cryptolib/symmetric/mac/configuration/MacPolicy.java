/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.configuration;

/**
 * Configuration policy for creating a MAC.
 *
 * <p>Implementations must be immutable.
 */
public interface MacPolicy {

	/**
	 * Returns the {@link ConfigMacAlgorithmAndProvider} that should be used when creating a MAC from some data.
	 *
	 * @return a provider.
	 */
	ConfigMacAlgorithmAndProvider getMacAlgorithmAndProvider();
}
