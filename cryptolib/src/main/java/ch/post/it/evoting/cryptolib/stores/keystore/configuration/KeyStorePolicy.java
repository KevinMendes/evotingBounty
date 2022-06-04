/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

/**
 * Interface which specifies the methods that should be implemented by a specific type of {@link KeyStorePolicy}.
 */
public interface KeyStorePolicy {

	/**
	 * Retrieves the {@link ConfigKeyStoreSpec} that encapsulates the key store specification.
	 *
	 * @return the {@link ConfigKeyStoreSpec}.
	 */
	ConfigKeyStoreSpec getKeyStoreSpec();
}
