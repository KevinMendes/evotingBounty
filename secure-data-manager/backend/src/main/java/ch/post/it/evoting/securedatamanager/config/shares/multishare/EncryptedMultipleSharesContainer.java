/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.multishare;

import java.security.PublicKey;
import java.util.Arrays;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.securedatamanager.config.shares.shares.EncryptedShare;
import ch.post.it.evoting.securedatamanager.config.shares.shares.SharesCrypto;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

/**
 * Note: this class is based on the class {@link EncryptedShare}. This class serves the same purpose for MultipleSharesContainer as EncryptedShare
 * does for Share.
 */
public class EncryptedMultipleSharesContainer {

	// public part
	private final byte[] encryptedShare;

	// public part
	private final byte[] encryptedShareSignature;

	// cryptographic helper
	private final SharesCrypto sharesCrypto;

	/**
	 * Constructor in decryption mode: The information is retrieved from the smartcard, and this class will be used to validate and decrypt the
	 * share.
	 * <p>
	 * The constructor will verify the signature before finishing, and throw a {@link SharesException} if the signature is not correct. See {@link
	 * #decrypt(byte[])}.
	 *
	 * @param encryptedShare          : byte[] with the encrypted share.
	 * @param encryptedShareSignature byte[] with a signature over the encryptedShare
	 * @param boardPublic             : Public key to verify the encryptedShareSignature
	 * @throws SharesException if the signature is not correct
	 */
	public EncryptedMultipleSharesContainer(final byte[] encryptedShare, final byte[] encryptedShareSignature, final PublicKey boardPublic)
			throws SharesException {

		this.sharesCrypto = new SharesCrypto();
		this.encryptedShare = clone(encryptedShare);
		this.encryptedShareSignature = clone(encryptedShareSignature);
		if (!sharesCrypto.verifyShare(this.encryptedShare, this.encryptedShareSignature, boardPublic)) {
			throw new SharesException("This share does not belong to this board");
		}
	}

	/**
	 * Use the secret key serialized form to decrypt the content of the share.
	 * <p>
	 * The caller is responsible for the secret key life cycle. This method will not make any copies of it.
	 *
	 * @param secretKeyBytes The serialized secret key.
	 * @return The decrypted {@link Share} if the key is correct.
	 */
	public MultipleSharesContainer decrypt(final byte[] secretKeyBytes) throws SharesException {
		if (secretKeyBytes == null) {
			return null;
		}
		final byte[] shareDecrypted = sharesCrypto.decryptShare(encryptedShare, secretKeyBytes);
		return new MultipleSharesContainer(shareDecrypted, MultipleSharesContainer.getModulusFromSerializedData(shareDecrypted));
	}

	/**
	 * Overwrite all the object attributes with 0x00 in memory. This method must always be called in order to guarantee no memory analysis can reveal
	 * the secret.
	 */
	public void destroy() {
		if (encryptedShare != null) {
			Arrays.fill(encryptedShare, (byte) 0x00);
		}
		if (encryptedShareSignature != null) {
			Arrays.fill(encryptedShareSignature, (byte) 0x00);
		}
	}

	/**
	 * Return the encrypted share content as a byte[].
	 *
	 * @return Returns the encryptedShare.
	 */
	public byte[] getEncryptedShare() {
		return encryptedShare.clone();
	}

	private byte[] clone(final byte[] value) {
		return value == null ? null : value.clone();
	}

}
