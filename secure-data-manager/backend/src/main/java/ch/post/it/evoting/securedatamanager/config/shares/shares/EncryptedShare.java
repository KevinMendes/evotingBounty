/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

/**
 * Representation of the encrypted share contents. It has a public part and a private part. The public part consists of the encrypted share, and a
 * signature over the encryption of the share. The private part is the secret key that encrypts the share. The method {@link #destroy()} must always
 * be called just after finishing usage to make sure no trace of the secret key is left on memory.
 */
public class EncryptedShare {
	// public part
	private final byte[] encryptedShareBytes;
	// public part
	private final byte[] encryptedShareSignature;
	// private part
	private byte[] secretKeyBytes;
	// cryptographic helper
	private SharesCrypto sharesCrypto;

	private ThresholdSecretSharingService thresholdSecretSharingService;

	/**
	 * Constructor in decipher mode: The information is retrieved from the smartcard, and this class will be used to validate and decipher the share.
	 * <p>
	 * The constructor will verify the signature before finishing, and throw a {@link SharesException} if the signature is not correct. See {@link
	 * #decrypt(byte[])}.
	 *
	 * @param encryptedShareBytes          byte[] with the encrypted share.
	 * @param encryptedShareSignature byte[] with a signature over the encryptedShare
	 * @param boardPublic             Public key to verify the encryptedShareSignature
	 * @throws SharesException if the signature is not correct
	 */
	public EncryptedShare(final byte[] encryptedShareBytes, final byte[] encryptedShareSignature, final PublicKey boardPublic) throws SharesException {

		initServices();

		this.encryptedShareBytes = encryptedShareBytes.clone();
		this.encryptedShareSignature = encryptedShareSignature.clone();

		if (!sharesCrypto.verifyShare(this.encryptedShareBytes, this.encryptedShareSignature, boardPublic)) {
			throw new SharesException("This share does not belong to this board");
		}
	}

	/**
	 * Constructor in encryption mode: The information to encrypt is provided in the {@link Share} object, and the {@link PrivateKey} is used to
	 * encrypt it. The constructor generates a {@link SecretKey} which is used to encrypt the {@link Share} content.
	 * <p>
	 * It is the responsibility of the caller to control the {@link PrivateKey} and {@link Share} life cycles, which contain sensitive information.
	 * This method will not make any copy of them.
	 *
	 * @param share        the {@link Share} to encrypt.
	 * @param boardPrivate the {@link PrivateKey} of the board to sign the {@link Share}
	 */
	public EncryptedShare(final Share share, final PrivateKey boardPrivate) {
		initServices();

		final SecretKey shareSecretKey = sharesCrypto.generateSecretKey();
		secretKeyBytes = sharesCrypto.serializeSecretKey(shareSecretKey);
		encryptedShareBytes = sharesCrypto.encryptShare(thresholdSecretSharingService.serialize(share), shareSecretKey);
		encryptedShareSignature = sharesCrypto.signShare(encryptedShareBytes, boardPrivate);
	}

	/**
	 * Use the secret key serialized form to decipher the content of the share.
	 * <p>
	 * The caller is responsible for the secret key life cycle. This method will not make any copies of it.
	 *
	 * @param secretKeyBytes The serialized secret key.
	 * @return The deciphered {@link Share} if the key is correct.
	 * @throws SharesException if the share cannot be decoded from the provided byte[]
	 */
	public Share decrypt(final byte[] secretKeyBytes) throws SharesException {
		if (secretKeyBytes == null) {
			return null;
		}
		try {
			return thresholdSecretSharingService.deserialize(sharesCrypto.decryptShare(encryptedShareBytes, secretKeyBytes));
		} catch (final GeneralCryptoLibException e) {
			throw new SharesException("Exception while decrypting the share.", e);
		}
	}

	/**
	 * Overwrite all the object attributes with 0x00 in memory. This method guarantees that no memory analysis will reveal the secret values.
	 */
	public void destroy() {
		if (secretKeyBytes != null) {
			Arrays.fill(secretKeyBytes, (byte) 0x00);
		}
		if (encryptedShareBytes != null) {
			Arrays.fill(encryptedShareBytes, (byte) 0x00);
		}
		if (encryptedShareSignature != null) {
			Arrays.fill(encryptedShareSignature, (byte) 0x00);
		}
	}

	/**
	 * Return the secret key in byte array format. Only store the secret key in the cryptographic token, and on the private part. You MUST NOT store,
	 * make copies, or log the secret key.
	 *
	 * @return Returns the secretKeyBytes.
	 */
	public byte[] getSecretKeyBytes() {
		return secretKeyBytes.clone();
	}

	/**
	 * Return the encrypted share content as a byte[].
	 *
	 * @return Returns the encryptedShare.
	 */
	public byte[] getEncryptedShareBytes() {
		return encryptedShareBytes.clone();
	}

	/**
	 * Return the encrypted share signature as a byte[].
	 *
	 * @return Returns the encryptedShareSignature.
	 */
	public byte[] getEncryptedShareSignature() {
		return encryptedShareSignature.clone();
	}

	private void initServices() {
		this.thresholdSecretSharingService = new ThresholdSecretSharingService();
		this.sharesCrypto = new SharesCrypto();
	}

}
