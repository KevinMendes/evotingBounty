/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SecretKeyPolicy;

/**
 * Configuration policy for asymmetrically encrypting and decrypting.
 *
 * <p>Implementations must be immutable.
 */
public interface AsymmetricCipherPolicy extends SecureRandomPolicy, SecretKeyPolicy, SymmetricCipherPolicy {

	/**
	 * Returns configuration for symmetric encryption.
	 *
	 * @return configuration.
	 */
	ConfigAsymmetricCipherAlgorithmAndSpec getAsymmetricCipherAlgorithmAndSpec();
}
