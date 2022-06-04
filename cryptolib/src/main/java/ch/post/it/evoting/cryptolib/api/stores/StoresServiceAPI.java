/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.stores;

import java.io.InputStream;
import java.security.KeyStore;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;

/**
 * Defines the API for using stores.
 */
public interface StoresServiceAPI {

	/**
	 * Creates a new {@link java.security.KeyStore} instance, which can be used to store and access cryptographic data related to keys.
	 *
	 * @param type the type of {@link java.security.KeyStore} being created.
	 * @return the newly created {@link java.security.KeyStore}.
	 * @throws GeneralCryptoLibException if the key store type is null or the key store creation process fails.
	 */
	KeyStore createKeyStore(KeyStoreType type) throws GeneralCryptoLibException;

	/**
	 * Loads an existing {@link java.security.KeyStore} instance, which can be used to store and access cryptographic data related to keys.
	 *
	 * @param type     the type of {@link java.security.KeyStore} being loaded.
	 * @param inStream the input stream from which the {@link java.security.KeyStore} is loaded.
	 * @param password the password to access the {@link java.security.KeyStore}
	 * @return the loaded {@link java.security.KeyStore}.
	 * @throws GeneralCryptoLibException if the key store type is null, the key store cannot be read, the password is blank or the key store loading
	 *                                   process fails.
	 */
	KeyStore loadKeyStore(KeyStoreType type, InputStream inStream, char[] password) throws GeneralCryptoLibException;
}
