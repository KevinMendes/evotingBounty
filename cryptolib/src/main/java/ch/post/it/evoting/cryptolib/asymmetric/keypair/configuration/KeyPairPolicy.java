/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

import ch.post.it.evoting.cryptolib.asymmetric.keypair.factory.CryptoKeyPairGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * Configuration policy to construct objects of type {@link CryptoKeyPairGenerator}
 *
 * <p>Implementations must be immutable.
 */
public interface KeyPairPolicy extends SecureRandomPolicy, SigningKeyPairPolicy, EncryptionKeyPairPolicy {
}
