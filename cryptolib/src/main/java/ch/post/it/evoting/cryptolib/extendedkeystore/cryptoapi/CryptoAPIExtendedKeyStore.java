/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi;

import java.io.OutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.List;

import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;

/**
 * Provides functionality to perform operations with a key store.
 */
public interface CryptoAPIExtendedKeyStore {

	/**
	 * Assigns the given {@link PrivateKey} and the given {@link Certificate} chain to the given alias, protecting it with the given password.
	 *
	 * @param alias      the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param privateKey the {@link PrivateKey} to be associated with the alias.
	 * @param password   the password used to seal the private key and the chain in the store. For security reasons, the password must contain a
	 *                   minimum of 16 characters.
	 * @param chain      the {@link Certificate} chain for the corresponding {@link PrivateKey}. The chain should contain the leaf at the first
	 *                   position of the array.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#setKeyEntry(String, java.security.Key, char[], Certificate[])
	 */
	void setPrivateKeyEntry(final String alias, final PrivateKey privateKey, final char[] password, final Certificate[] chain)
			throws GeneralCryptoLibException;

	/**
	 * Assigns the given {@link SecretKey} to the given alias, protecting it with the given password.
	 *
	 * @param alias     the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param secretKey the {@link SecretKey} to be associated with the alias.
	 * @param password  the password used to seal the {@link SecretKey} in the store. For security reasons, the password must contain a minimum of 16
	 *                  characters.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#setKeyEntry(String, Key, char[], Certificate[])
	 */
	void setSecretKeyEntry(final String alias, final SecretKey secretKey, final char[] password) throws GeneralCryptoLibException;

	/**
	 * Assigns the given {@link ElGamalPrivateKey} to the given alias, protecting it with the given password.
	 *
	 * @param alias      the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param privateKey the {@link ElGamalPrivateKey} to be associated with the alias.
	 * @param password   the password used to seal the {@link ElGamalPrivateKey} in the store. For security reasons, the password must contain a
	 *                   minimum of 16 characters.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#setKeyEntry(String, Key, char[], Certificate[])
	 */
	void setElGamalPrivateKeyEntry(final String alias, final ElGamalPrivateKey privateKey, final char[] password) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link Certificate} chain associated with the given alias.
	 *
	 * @param alias the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @return the chain of certificates associated to this alias as a array of {@link Certificate} array. The leaf is to be found at the first
	 * position of the array.
	 * @throws GeneralCryptoLibException if argument is invalid.
	 * @see java.security.KeyStore#getCertificateChain(String)
	 */
	Certificate[] getCertificateChain(final String alias) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link PrivateKey} associated with the given alias, using the given password to recover it.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param password the password used to seal the {@link PrivateKey} in the store. For security reasons, the password must contain a minimum of 16
	 *                 characters.
	 * @return the {@link PrivateKey}.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#getKey(String, char[])
	 */
	PrivateKey getPrivateKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link SecretKey} associated with the given alias, using the given password to recover it.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param password the password used to seal the key in the store. For security reasons, the password must contain a minimum of 16 characters.
	 * @return the {@link SecretKey}.
	 * @throws GeneralCryptoLibException if the retrieving of secret key failed.
	 * @see java.security.KeyStore#getKey(String, char[])
	 */
	SecretKey getSecretKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link ElGamalPrivateKey} associated with the given alias, using the given password to recover it.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param password the password used to seal the {@link ElGamalPrivateKey} in the store. For security reasons, the password must contain a minimum
	 *                 of 16 characters.
	 * @return the ElGamal Private Key.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 */
	ElGamalPrivateKey getElGamalPrivateKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException;

	/**
	 * Returns a list of the {@link SecretKey} aliases that the store contains.
	 *
	 * @return the list of secret key aliases.
	 */
	List<String> getSecretKeyAliases();

	/**
	 * Returns a list of the {@link PrivateKey} aliases that the store contains.
	 *
	 * @return the list of private key aliases.
	 */
	List<String> getPrivateKeyAliases();

	/**
	 * Returns a list of the {@link ElGamalPrivateKey} aliases that the store contains.
	 *
	 * @return the list of ElGamal Private Key aliases.
	 */
	List<String> getElGamalPrivateKeyAliases();

	/**
	 * Returns the store in JSON format ready to be sent to client side.
	 *
	 * <p>The following example contains two secret keys ciphered and encoded in base64, the salt used
	 * to derive password also encoded in base64 and the store encoded in base64 too.
	 *
	 * <pre>
	 *  {
	 * "salt": "gGUEPTHzqSzcbmIKOOu4WHUdKUo=",
	 * "secrets": [
	 * {
	 * "myAliasSymmetric2": "6USvcQRsn55WkFyAtlneO+ZPNdwxe69rsMVHKcj633hPO2is5r17aGj6wnpLJCOuU2b/b3O0KZTTySR/DCRwCCE="
	 * },
	 * {
	 * "myAliasSymmetric": "W3F4H6Pp7oG+RdB3NXIuy6MxPUWNtXZUpSU4Qj3kcm8BZTYZphQtkBrQn83wvi2GUko7NE0Bbr4Brde829n+8Q=="
	 * }
	 * ],
	 * "store": "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"
	 * }
	 * </pre>
	 *
	 * <p>This function is non-deterministic, several calls on the same key store and password can
	 * produce different values of the store attribute which is an encrypted collection of the private keys.
	 *
	 * @param password the password to seal the internal store
	 * @return the store in JSON.
	 * @throws GeneralCryptoLibException if the input to the helper that performs the storage to JSON format is invalid.
	 */
	default String toJSON(final char[] password) throws GeneralCryptoLibException {
		throw new UnsupportedOperationException("toJSON");
	}

	/**
	 * Stores this key store to the given {@link OutputStream} {@code out}, and protects its integrity with the given password.
	 *
	 * @param out      the {@link OutputStream} to which this key store is written.
	 * @param password the password used to seal the store.
	 * @throws GeneralCryptoLibException if the key store output stream is null or if the key store password is null or consists only of white
	 *                                   spaces.
	 * @see java.security.KeyStore#store(OutputStream, char[])
	 */
	void store(final OutputStream out, final char[] password) throws GeneralCryptoLibException;
}
