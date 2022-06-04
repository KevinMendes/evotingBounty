/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model;

/**
 * Class for storing encrypted start voting key.
 */
public class EncryptedSVK {

	private final String encryptedSVK;

	/**
	 * Constructor.
	 *
	 * @param encryptedSVK the encrypted start voting key.
	 */
	public EncryptedSVK(String encryptedSVK) {
		super();
		this.encryptedSVK = encryptedSVK;
	}

	/**
	 * Returns the current value of the field encryptedSVK.
	 *
	 * @return Returns the encryptedSVK.
	 */
	public String getEncryptedSVK() {
		return encryptedSVK;
	}
}
