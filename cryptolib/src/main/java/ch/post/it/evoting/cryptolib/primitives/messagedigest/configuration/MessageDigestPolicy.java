/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration;

/**
 * Configuration policy for creating a message digest.
 *
 * <p>Implementations must be immutable.
 */
public interface MessageDigestPolicy {

	/**
	 * Returns the {@link ConfigMessageDigestAlgorithmAndProvider} that should be used when creating a message digest from some data.
	 *
	 * @return The {@link ConfigMessageDigestAlgorithmAndProvider}.
	 */
	ConfigMessageDigestAlgorithmAndProvider getMessageDigestAlgorithmAndProvider();
}
