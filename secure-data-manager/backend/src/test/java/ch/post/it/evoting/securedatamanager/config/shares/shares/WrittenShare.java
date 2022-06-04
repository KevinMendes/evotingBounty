/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares;

import java.util.Base64;

/**
 * Meant to hold the share values for testing. Makes copies of the values since part of the testing consists on deleting the values.
 */
public class WrittenShare {

	// private part
	private final byte[] secretKeyBytes;

	// public part
	private final byte[] encryptedShare;

	// public part
	private final byte[] encryptedShareSignature;

	public WrittenShare(final byte[] secretKeyBytes, final byte[] encryptedShare, final byte[] encryptedShareSignature) {

		this.secretKeyBytes = secretKeyBytes.clone();
		this.encryptedShare = encryptedShare.clone();
		this.encryptedShareSignature = encryptedShareSignature.clone();
	}

	/**
	 * @return Returns the secretKeyBytes.
	 */
	public byte[] getSecretKeyBytes() {
		return secretKeyBytes.clone();
	}

	/**
	 * @return Returns the encryptedShare.
	 */
	public byte[] getEncryptedShare() {
		return encryptedShare.clone();
	}

	/**
	 * @return Returns the encryptedShareSignature.
	 */
	public byte[] getEncryptedShareSignature() {
		return encryptedShareSignature.clone();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WrittenShare [_secretKeyBytes=" + Base64.getEncoder().encodeToString(secretKeyBytes) + ", _encryptedShare=" + Base64.getEncoder()
				.encodeToString(encryptedShare) + ", _encryptedShareSignature=" + Base64.getEncoder().encodeToString(encryptedShareSignature) + "]";
	}

}
