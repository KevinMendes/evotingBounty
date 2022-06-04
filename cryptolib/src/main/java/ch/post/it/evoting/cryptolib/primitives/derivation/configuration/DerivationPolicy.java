/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;

/**
 * An interface which extends derivation policies as well as the {@link SecureRandomPolicy}.
 */
public interface DerivationPolicy extends KDFDerivationPolicy, PBKDFDerivationPolicy, SecureRandomPolicy {
}
