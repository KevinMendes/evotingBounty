/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.extendedkeystore;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyStore.PasswordProtection;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;

/**
 * This interface defines public operations that can be done with stores.
 */
public interface KeyStoreService {

	/**
	 * Creates an empty Key store ready to fill up with data.
	 *
	 * <p>In order to physically store the returned key store a further call to {@link
	 * CryptoAPIExtendedKeyStore#store(java.io.OutputStream, char[])} method should be done.
	 *
	 * @return the key store created.
	 */
	CryptoAPIExtendedKeyStore createKeyStore();

	/**
	 * Loads a key store.
	 *
	 * <p>A password must be given to unlock the store.
	 *
	 * @param in       the stream from the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @param password the password used to check the integrity of the store. For security reasons, the password must contain a minimum of 16
	 *                 characters.
	 * @return the key store loaded.
	 * @throws GeneralCryptoLibException if there is a problem reading the store or if there is an I/O or format problem with the key store data.
	 */
	CryptoAPIExtendedKeyStore loadKeyStore(InputStream in, final PasswordProtection password) throws GeneralCryptoLibException;

	/**
	 * Loads a key store.
	 *
	 * <p> A password must be given to unlock the store.</p>
	 *
	 * @param in       the stream from which the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @param password the password used to check the integrity of the store.
	 * @return the key store loaded.
	 * @throws GeneralCryptoLibException if there is a problem reading the store or if there is an I/O or format problem with the key store data.
	 */
	CryptoAPIExtendedKeyStore loadKeyStore(InputStream in, char[] password) throws GeneralCryptoLibException;

	/**
	 * Loads a key store from the provided path.
	 *
	 * <p>A password must be given to unlock the store.</p>
	 *
	 * @param path     the path from which the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @param password the password used to check the integrity of the store.
	 * @return the key store loaded.
	 * @throws GeneralCryptoLibException if there is a problem reading the store or if there is an I/O or format problem with the key store data.
	 */
	CryptoAPIExtendedKeyStore loadKeyStore(Path path, char[] password) throws GeneralCryptoLibException, FileNotFoundException;

	/**
	 * Loads a key store. The store is expected in JSON format, see {@link CryptoAPIExtendedKeyStore#toJSON(char[])}.
	 *
	 * @param in       the stream from the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @param password the password used to check the integrity of the store. For security reasons, the password must contain a minimum of 16
	 *                 characters.
	 * @return the key store loaded.
	 * @throws GeneralCryptoLibException if there is a problem reading the store or if there is an I/O or format problem with the key store data.
	 */
	CryptoAPIExtendedKeyStore loadKeyStoreFromJSON(InputStream in, PasswordProtection password) throws GeneralCryptoLibException;

	/**
	 * Formats the given {@link CryptoAPIExtendedKeyStore} to JSON format.
	 *
	 * @param in the stream from the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @return the given {@link CryptoAPIExtendedKeyStore} in JSON format.
	 * @throws GeneralCryptoLibException if there is a problem reading the store or if there is an I/O or format problem with the key store data.
	 */
	String formatKeyStoreToJSON(InputStream in) throws GeneralCryptoLibException;
}
