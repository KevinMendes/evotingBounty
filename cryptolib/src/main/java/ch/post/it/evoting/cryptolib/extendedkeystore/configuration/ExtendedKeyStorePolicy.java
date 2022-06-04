/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.configuration;

import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SecretKeyPolicy;

/**
 * Interface which specifies the methods that should be implemented by specific {@code StorePolicy}.
 */
public interface ExtendedKeyStorePolicy extends DerivationPolicy, SymmetricCipherPolicy, SecretKeyPolicy {

	ConfigExtendedKeyStoreTypeAndProvider getStoreTypeAndProvider();
}
