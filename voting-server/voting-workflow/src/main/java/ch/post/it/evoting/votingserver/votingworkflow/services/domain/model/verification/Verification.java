/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification;

/**
 * Contains the verification data of a voter.
 */
public class Verification {

	/**
	 * The identifier of the verification.
	 */
	private String id;

	/**
	 * Data containing the keystore required to calculate the partial return codes. Typically, a key
	 * store.
	 */
	private String verificationCardKeystore;

	/**
	 * Returns the current value of the field id.
	 *
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the value of the field id.
	 *
	 * @param id The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the value of the field verificationCardKeystore.
	 *
	 * @return the verificationCardKeystore
	 */
	public String getVerificationCardKeystore() {
		return verificationCardKeystore;
	}

	/**
	 * Sets the value of verificationCardKeystore field.
	 *
	 * @param verificationCardKeystore The masking value to set
	 */
	public void setVerificationCardKeystore(String verificationCardKeystore) {
		this.verificationCardKeystore = verificationCardKeystore;
	}

}
