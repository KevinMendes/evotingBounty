/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.security.PrivateKey;
import java.security.cert.Certificate;

public interface KeystoreReader {

	/**
	 * @return Root certificate for the specified node, if found in the keystore; null if not found therein.
	 */
	Certificate readRootCertificate();

	/**
	 * @param nodeIdentifier Node whose signing certificate is tbeing requested.
	 * @return Signing certificate for the specified node, if found in the keystore; null if not found therein.
	 */
	Certificate readSigningCertificate(NodeIdentifier nodeIdentifier);

	/**
	 * @param nodeIdentifier Node whose encryption certificate is being requested.
	 * @return Encryptiong certificate for the specified node, if found in the keystore; null if not found therein.
	 */
	Certificate readEncryptionCertificate(NodeIdentifier nodeIdentifier);

	/**
	 * @param nodeIdentifier    Node whose signing private key is being requested.
	 * @param keystorePasswords Passwords to access the keystore.
	 * @return Signing private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readSigningPrivateKey(NodeIdentifier nodeIdentifier, KeystorePasswords keystorePasswords);

	/**
	 * @param nodeIdentifier   Node whose signing certificate is being requested.
	 * @param keystorePassword Password to access both the keystore and the requested private key.
	 * @return Signing private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readSigningPrivateKey(NodeIdentifier nodeIdentifier, String keystorePassword);

	/**
	 * @param nodeIdentifier            Node whose signing private key is being requested.
	 * @param keystorePassword          Password to access the keystore.
	 * @param signingPrivateKeyPassword Password to access the requested private key within the keystore.
	 * @return Signing private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readSigningPrivateKey(NodeIdentifier nodeIdentifier, String keystorePassword, String signingPrivateKeyPassword);

	/**
	 * @param nodeIdentifier    Node whose encryption private key is being requested.
	 * @param keystorePasswords Passwords to access the keystore.
	 * @return Encryption private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readEncryptionPrivateKey(NodeIdentifier nodeIdentifier, KeystorePasswords keystorePasswords);

	/**
	 * @param nodeIdentifier   Node whose encryption private key is being requested.
	 * @param keystorePassword Password to access both the keystore and the requested private key.
	 * @return Encryption private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readEncryptionPrivateKey(NodeIdentifier nodeIdentifier, String keystorePassword);

	/**
	 * @param nodeIdentifier               Node whose encryption private key is being requested.
	 * @param keystorePassword             Password to access the keystore.
	 * @param encryptionPrivateKeyPassword Password to access the requested private key within the keystore.
	 * @return Encryption private key for the specified node, if found in the keystore; null if not found therein.
	 */
	PrivateKey readEncryptionPrivateKey(NodeIdentifier nodeIdentifier, String keystorePassword, String encryptionPrivateKeyPassword);

}
