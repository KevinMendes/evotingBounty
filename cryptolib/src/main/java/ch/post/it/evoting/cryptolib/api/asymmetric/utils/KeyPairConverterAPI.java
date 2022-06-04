/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.asymmetric.utils;

import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * This class allows to import and export {@link PrivateKey} and {@link PublicKey} objects to and from Strings in PEM format.
 *
 * <p><b>PEM</b>, which stands for Privacy Enhanced Mail, is a container format for either public or
 * private keys, as well as certificates. It basically is a Base64 encoded DER (Distinguished Encoding Rules) object, which is enclosed within two
 * clauses. For instance, by:
 *
 * <ul>
 *   <li>"-----BEGIN RSA PRIVATE KEY-----" and "-----END RSA PRIVATE KEY-----".
 *   <li>"-----BEGIN PUBLIC KEY-----" and "-----END PUBLIC KEY-----"
 * </ul>
 * <p>
 * depending on the encoded object. The number of dashes is meaningful, and thus it should be
 * correct.
 */
public interface KeyPairConverterAPI {

	/**
	 * Constructs a signing public key given a key in PEM format.
	 *
	 * @param publicKeyPem the string in PEM format.
	 * @return a public key.
	 * @throws GeneralCryptoLibException if {@code publicKeyPem} is invalid.
	 */
	PublicKey getPublicKeyForSigningFromPem(final String publicKeyPem) throws GeneralCryptoLibException;

	/**
	 * Constructs an encrypting public key given a key in PEM format.
	 *
	 * @param publicKeyPem the string in PEM format.
	 * @return a public key
	 * @throws GeneralCryptoLibException if {@code publicKeyPem} is invalid.
	 */
	PublicKey getPublicKeyForEncryptingFromPem(final String publicKeyPem) throws GeneralCryptoLibException;

	/**
	 * Constructs a signing private key given a key in PEM format.
	 *
	 * @param privateKeyPem the private key as a string in PEM format.
	 * @return a private key.
	 * @throws GeneralCryptoLibException if {@code privateKeyPem} is invalid.
	 */
	PrivateKey getPrivateKeyForSigningFromPem(final String privateKeyPem) throws GeneralCryptoLibException;

	/**
	 * Constructs an encrypting private key given a key in PEM format.
	 *
	 * @param privateKeyPem the private key as a string in PEM format.
	 * @return a private key.
	 * @throws GeneralCryptoLibException if {@code privateKeyPem} is invalid.
	 */
	PrivateKey getPrivateKeyForEncryptingFromPem(final String privateKeyPem) throws GeneralCryptoLibException;

	/**
	 * Exports the signing public key to a string in PEM format.
	 *
	 * @param publicKey the public key.
	 * @return the string in PEM format.
	 * @throws GeneralCryptoLibException if {@code PublicKey} is invalid.
	 */
	String exportPublicKeyForSigningToPem(final PublicKey publicKey) throws GeneralCryptoLibException;

	/**
	 * Exports the encrypting public key to a string in PEM format.
	 *
	 * @param publicKey the public key.
	 * @return the string in PEM format.
	 * @throws GeneralCryptoLibException if {@code PublicKey} is invalid.
	 */
	String exportPublicKeyForEncryptingToPem(final PublicKey publicKey) throws GeneralCryptoLibException;

	/**
	 * Exports the signing private key to a string in PEM format.
	 *
	 * @param privateKey the private key.
	 * @return the string in PEM format.
	 * @throws GeneralCryptoLibException if {@code PrivateKey} is invalid.
	 */
	String exportPrivateKeyForSigningToPem(final PrivateKey privateKey) throws GeneralCryptoLibException;

	/**
	 * Exports the encrypting private key to a string in PEM format.
	 *
	 * @param privateKey the private key.
	 * @return the string in PEM format.
	 * @throws GeneralCryptoLibException if {@code PrivateKey} is invalid.
	 */
	String exportPrivateKeyForEncryptingToPem(final PrivateKey privateKey) throws GeneralCryptoLibException;
}
