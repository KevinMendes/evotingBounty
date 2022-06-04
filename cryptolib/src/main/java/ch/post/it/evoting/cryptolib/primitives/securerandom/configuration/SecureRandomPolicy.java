/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.configuration;

/**
 * Configuration policy for creating instances of generators {@link java.security.SecureRandom}.
 *
 * <p>Implementations must be immutable.
 */
public interface SecureRandomPolicy {

	/**
	 * Returns the {@link ConfigSecureRandomAlgorithmAndProvider} that should be used when getting instances of {@link java.security.SecureRandom}.
	 *
	 * @return The {@link ConfigSecureRandomAlgorithmAndProvider}.
	 */
	ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider();
}
