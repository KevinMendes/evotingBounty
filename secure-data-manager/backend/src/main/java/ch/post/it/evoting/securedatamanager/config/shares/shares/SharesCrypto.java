/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

/**
 * Cryptographic operations on shares contents. This class deals with symmetric encryption of the share contents, signature and validation of the
 * encrypted share and secret key generation.
 */
public class SharesCrypto {

	private static final String SECRET_KEY_ALGORITHM = "AES";
	private final SymmetricServiceAPI symmetricService;
	private final AsymmetricServiceAPI asymmetricService;

	public SharesCrypto() {
		this.symmetricService = new SymmetricService();
		this.asymmetricService = new AsymmetricService();
	}

	/**
	 * Build a {@link SecretKey} to encrypt the share secret.
	 *
	 * @return A {@link SecretKey}.
	 */
	public SecretKey generateSecretKey() {
		return symmetricService.getSecretKeyForEncryption();
	}

	/**
	 * Serialize a {@link SecretKey} to store it in the private section of the share.
	 *
	 * @param secretKey The {@link SecretKey} to serialize.
	 * @return the byte[] of the {@link SecretKey} serialized.
	 */
	public byte[] serializeSecretKey(final SecretKey secretKey) {
		return secretKey.getEncoded();
	}

	/**
	 * Symmetric encryption of the share representation to store in the token public section.
	 *
	 * @param share          The serialized representation of the share, in clear.
	 * @param shareSecretKey The {@link SecretKey} to encrypt the share.
	 * @return the byte[] of the encrypted share.
	 */
	public byte[] encryptShare(final byte[] share, final SecretKey shareSecretKey) {
		try {
			return symmetricService.encrypt(shareSecretKey, share);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalStateException("Failed to encrypt share.", e);
		}
	}

	/**
	 * Sign the encrypted share bytes to store them in the token public section.
	 *
	 * @param encryptedShare the byte[] representation of the encrypted share.
	 * @param boardPrivate   the private key of the board, so that anyone can check the share was actually made by the board.
	 * @return The byte[] with the signature of the encrypted share bytes.
	 */
	public byte[] signShare(final byte[] encryptedShare, final PrivateKey boardPrivate) {
		try {
			return asymmetricService.sign(boardPrivate, encryptedShare);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalStateException("Failed to sign encrypted share bytes.", e);
		}
	}

	/**
	 * Validate a share is signed by provided the board key.
	 *
	 * @param encryptedShare The encrypted share bytes, the data do check.
	 * @param shareSignature The signature bytes.
	 * @param boardPublic    The public key of the board that signed the share.
	 * @return true if the signature is valid, false otherwise.
	 * @throws SharesException if the validation fails due to the kind of keys, length of signature, etc.
	 */
	public boolean verifyShare(final byte[] encryptedShare, final byte[] shareSignature, final PublicKey boardPublic) throws SharesException {

		try {
			return asymmetricService.verifySignature(shareSignature, boardPublic, encryptedShare);
		} catch (final GeneralCryptoLibException e) {
			throw new SharesException(e);
		}
	}

	/**
	 * Decipher the encrypted share.
	 *
	 * @param encryptedShare The byte[] of the encrypted share.
	 * @param secretKeyBytes The byte[] of the secret key that encrypts the share.
	 * @return the byte[] of the deciphered share.
	 */
	public byte[] decryptShare(final byte[] encryptedShare, final byte[] secretKeyBytes) {
		try {
			return symmetricService.decrypt(deserializeSecretKey(secretKeyBytes), encryptedShare);
		} catch (final GeneralCryptoLibException e) {
			throw new IllegalStateException("Failed to decipher the encrypted share.", e);
		}
	}

	private SecretKey deserializeSecretKey(final byte[] secretKeyBytes) {
		return new SecretKeySpec(secretKeyBytes, SECRET_KEY_ALGORITHM);
	}

}
