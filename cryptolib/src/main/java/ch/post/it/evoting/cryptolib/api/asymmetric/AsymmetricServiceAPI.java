/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.asymmetric;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * An API for using cryptographic asymmetric activities.
 */
public interface AsymmetricServiceAPI {

	/**
	 * Generates a {@link java.security.KeyPair} to be used for signing data.
	 *
	 * @return a {@link java.security.KeyPair} that can be used for signing.
	 */
	KeyPair getKeyPairForSigning();

	/**
	 * Generates a {@link java.security.KeyPair} to be used for encryption.
	 *
	 * @return a {@link java.security.KeyPair} that can be used for encryption.
	 */
	KeyPair getKeyPairForEncryption();

	/**
	 * Encrypts the given data, using the given {@link java.security.PublicKey}.
	 *
	 * @param key  the {@link java.security.PublicKey} used for encryption.
	 * @param data the data to be encrypted.
	 * @return the encrypted data.
	 * @throws GeneralCryptoLibException if the public key or the data is null or empty or if the encryption process fails.
	 */
	byte[] encrypt(final PublicKey key, final byte[] data) throws GeneralCryptoLibException;

	/**
	 * Decrypts the given encrypted data, using the given {@link java.security.PrivateKey}.
	 *
	 * @param key  the {@link java.security.PrivateKey} used for decryption.
	 * @param data the data to be decrypted.
	 * @return the decrypted data.
	 * @throws GeneralCryptoLibException if the private key or the data is null or empty or if the decryption process fails.
	 */
	byte[] decrypt(final PrivateKey key, final byte[] data) throws GeneralCryptoLibException;

	/**
	 * Digitally signs the given data, using the given {@link java.security.PrivateKey}.
	 *
	 * @param key  the {@link java.security.PrivateKey} used to sign the data.
	 * @param data the collection of data to sign.
	 * @return the signature.
	 * @throws GeneralCryptoLibException if the private key or the data is null or empty or if the signature generation process fails.
	 */
	byte[] sign(final PrivateKey key, final byte[]... data) throws GeneralCryptoLibException;

	/**
	 * Verifies the given signature of the given data, using the given {@link java.security.PublicKey}.
	 *
	 * @param signature the signature.
	 * @param key       the {@link java.security.PublicKey} used to verify the signature.
	 * @param data      the collection of data that was signed.
	 * @return true if the signature is valid, false otherwise.
	 * @throws GeneralCryptoLibException if the signature, the public key or the data is null or empty or if the signature verification process
	 *                                   fails.
	 */
	boolean verifySignature(final byte[] signature, final PublicKey key, final byte[]... data) throws GeneralCryptoLibException;

	/**
	 * Returns a new instance of a {@link KeyPairConverterAPI}.
	 *
	 * @return the {@link KeyPairConverterAPI} instance.
	 */
	KeyPairConverterAPI getKeyPairConverter();

	/**
	 * Signs the data from the given {@link InputStream}, using the given {@link java.security.PrivateKey}.
	 *
	 * @param key the {@link java.security.PrivateKey} used to sign the data.
	 * @param in  the {@link InputStream} containing the data to be signed.
	 * @return the signature.
	 * @throws GeneralCryptoLibException if the private key is null or empty, the input data cannot be read or the signature generation process
	 *                                   fails.
	 */
	byte[] sign(PrivateKey key, InputStream in) throws GeneralCryptoLibException;

	/**
	 * Verifies the given signature of the data from the given {@link InputStream}, using the given {@link java.security.PublicKey}.
	 *
	 * @param signature the signature.
	 * @param key       the {@link java.security.PublicKey} used to verify the signature.
	 * @param in        the {@link InputStream} containing the data that was signed.
	 * @return true if the signature is valid, false otherwise.
	 * @throws GeneralCryptoLibException if the signature or the public key is null or empty or if the signature verification process fails.
	 */
	boolean verifySignature(byte[] signature, PublicKey key, InputStream in) throws GeneralCryptoLibException;
}
