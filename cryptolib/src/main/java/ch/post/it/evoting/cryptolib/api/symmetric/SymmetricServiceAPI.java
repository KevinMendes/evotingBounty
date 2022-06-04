/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.symmetric;

import java.io.InputStream;

import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * An API for using cryptographic symmetric activities.
 */
public interface SymmetricServiceAPI {

	/**
	 * Generates a {@link javax.crypto.SecretKey} that can be used for encryption.
	 *
	 * <p>The generated {@link javax.crypto.SecretKey} will be configured according to the properties
	 * of the service.
	 *
	 * @return a SecretKey that can be used for encryption.
	 */
	SecretKey getSecretKeyForEncryption();

	/**
	 * Generates a {@link javax.crypto.SecretKey} that can be used for HMAC.
	 *
	 * <p>The generated SecretKey will be configured according to the properties of the service.
	 *
	 * @return a {@link javax.crypto.SecretKey} that can be used for HMAC.
	 */
	SecretKey getSecretKeyForHmac();

	/**
	 * Encrypts the given plain text using the given {@link javax.crypto.SecretKey} and an internally generated random Initialization Vector (IV) of
	 * size given by the symmetric algorithm.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param data the plain text to be encrypted.
	 * @return the generated IV and the cipher text.
	 * @throws GeneralCryptoLibException if the key or the data is null or empty or if the encryption process fails.
	 */
	byte[] encrypt(final SecretKey key, final byte[] data) throws GeneralCryptoLibException;

	/**
	 * Decrypts the given data which contains the IV and the cipher text using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param data the IV and the cipher text.
	 * @return the plain text.
	 * @throws GeneralCryptoLibException if secret key algorithm is not equals to symmetric encryption algorithm.
	 * @throws GeneralCryptoLibException if the key or the data is null or empty or if the decryption process fails.
	 */
	byte[] decrypt(final SecretKey key, final byte[] data) throws GeneralCryptoLibException;

	/**
	 * Generates a MAC for the given data, using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param data the message to be authenticated.
	 * @return the generated message MAC.
	 * @throws GeneralCryptoLibException if the key or the data is null or empty or if the MAC generation fails.
	 */
	byte[] getMac(final SecretKey key, final byte[]... data) throws GeneralCryptoLibException;

	/**
	 * Generates a MAC from data readable from the given {@link InputStream}, using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param key the {@link javax.crypto.SecretKey} to use.
	 * @param in  the {@link InputStream} from which to read data.
	 * @return the generated message MAC.
	 * @throws GeneralCryptoLibException if the given key is null or contains no data.
	 * @throws GeneralCryptoLibException if key is null or empty, the data cannot be read from the input stream or the MAC generation process fails.
	 */
	byte[] getMac(final SecretKey key, InputStream in) throws GeneralCryptoLibException;

	/**
	 * Verifies that a given MAC is indeed the MAC for the given data, using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param key  the {@link javax.crypto.SecretKey} to use.
	 * @param mac  the MAC to be verified.
	 * @param data the message to be authenticated.
	 * @return true if the MAC is the MAC of the given data and SecretKey, false otherwise.
	 * @throws GeneralCryptoLibException if the key, the MAC or the data is null or empty or if the MAC verification process fails.
	 */
	boolean verifyMac(final SecretKey key, final byte[] mac, final byte[]... data) throws GeneralCryptoLibException;

	/**
	 * Verifies that a given MAC is indeed the MAC for the data readable from the given {@link InputStream}, using the given {@link
	 * javax.crypto.SecretKey}.
	 *
	 * @param key the {@link javax.crypto.SecretKey} to use.
	 * @param mac the MAC to be verified.
	 * @param in  the {@link InputStream} from which to read data.
	 * @return true if the MAC is the MAC of the data from the given input stream using the given secret key, false otherwise.
	 * @throws GeneralCryptoLibException if the key or the MAC is null or empty, the data cannot be read from the input stream or the MAC verification
	 *                                   process fails.
	 */
	boolean verifyMac(final SecretKey key, final byte[] mac, InputStream in) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link SecretKey} ready to use for generating/verifying a MAC, from a {@link CryptoAPIDerivedKey}.
	 *
	 * @param key the {@link CryptoAPIDerivedKey}.
	 * @return the {@link SecretKey} for MAC.
	 * @throws GeneralCryptoLibException if the key is null or empty.
	 */
	SecretKey getSecretKeyForMacFromDerivedKey(CryptoAPIDerivedKey key) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link SecretKey} ready to use for encrypt/decrypt, from a byte[].
	 *
	 * @param keyBytes the encoded key as a byte array.
	 * @return the {@link SecretKey} for encrypt/decrypt.
	 * @throws GeneralCryptoLibException if the key is null or empty.
	 */
	SecretKey getSecretKeyForEncryptionFromBytes(byte[] keyBytes) throws GeneralCryptoLibException;

	/**
	 * Returns the {@link SecretKey} ready to use for encrypt/decrypt, from a {@link CryptoAPIDerivedKey}.
	 *
	 * @param key the {@link CryptoAPIDerivedKey}.
	 * @return the {@link SecretKey} for encrypt/decrypt.
	 * @throws GeneralCryptoLibException if the key is null or empty.
	 */
	SecretKey getSecretKeyForEncryptionFromDerivedKey(CryptoAPIDerivedKey key) throws GeneralCryptoLibException;
}
