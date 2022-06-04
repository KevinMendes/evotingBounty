/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Enum which specifies the supported cryptographic properties when requesting an instance of a key store.
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigKeyStoreSpec {
	SUN(Provider.SUN),
	BC(Provider.BOUNCY_CASTLE),
	DEFAULT(Provider.DEFAULT);

	private final Provider provider;

	ConfigKeyStoreSpec(final Provider provider) {
		this.provider = provider;
	}

	public Provider getProvider() {
		return provider;
	}
}
