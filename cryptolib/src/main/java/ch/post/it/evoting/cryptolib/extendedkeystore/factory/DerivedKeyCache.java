/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

/**
 * Cache for derived keys.
 *
 * <p>Warning! The cache works correctly if and only if the derived key is fully determined by its
 * original password. It means that the client must use the same derivation algorithm with the same salt.
 */
interface DerivedKeyCache {
	/**
	 * Returns the key for a given password.
	 *
	 * @param password the password
	 * @return the key or {@code null} if the key does not exist.
	 */
	byte[] get(char[] password);

	/**
	 * Puts the key which protects the specified El Gamal private key
	 *
	 * @param alias    the private key alias
	 * @param password the password
	 * @param key      the key
	 */
	void putForElGamalPrivateKey(String alias, char[] password, byte[] key);

	/**
	 * Puts the key which is used to protect the key store.
	 *
	 * @param password the password
	 * @param key      the key.
	 */
	void putForKeyStore(char[] password, byte[] key);

	/**
	 * Puts the key which protects the specified private key.
	 *
	 * @param alias    the private key alias
	 * @param password the password
	 * @param key      the key
	 */
	void putForPrivateKey(String alias, char[] password, byte[] key);

	/**
	 * Puts the key which protects the specified secret key.
	 *
	 * @param alias    the secret key alias
	 * @param password the password
	 * @param key      the key.
	 */
	void putForSecretKey(String alias, char[] password, byte[] key);

	/**
	 * Removes the key which protects the specified El Gamal private key
	 *
	 * @param alias the privat key alias
	 */
	void removeForElGamalPrivateKey(String alias);

	/**
	 * Removes the key which protects the specified private key.
	 *
	 * @param alias the private key alias
	 */
	void removeForPrivateKey(String alias);

	/**
	 * Removes the key which protects the specified secret key.
	 *
	 * @param alias the secret key alias.
	 */
	void removeForSecretKey(String alias);
}
