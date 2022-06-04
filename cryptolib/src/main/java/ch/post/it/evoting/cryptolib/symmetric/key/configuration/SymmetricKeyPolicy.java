/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * Defines all of the methods that must be supported by a symmetric key.
 */
public interface SymmetricKeyPolicy extends SecretKeyPolicy, HmacSecretKeyPolicy, SecureRandomPolicy {
}
