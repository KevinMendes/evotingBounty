/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * Configuration policy for symmetrically encrypting and decrypting.
 *
 * <p>Implementations must be immutable.
 */
public interface SymmetricCipherPolicy extends SecureRandomPolicy {

	/**
	 * Provides configuration for symmetrically encrypting and decrypting.
	 *
	 * @return configuration.
	 */
	ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec();
}
