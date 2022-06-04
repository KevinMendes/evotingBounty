/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.authentication;

/**
 * Contains the credential data of a voter.
 */
public class Credential {

	/**
	 * The general identifier of the credential.
	 */
	private String id;

	/**
	 * Credential data to be sent to the user so he can authenticate. Typically, a key store.
	 */
	private String data;

	/**
	 * Returns the current value of the field data.
	 *
	 * @return Returns the data.
	 */
	public String getData() {
		return data;
	}

	/**
	 * Sets the value of the field data.
	 *
	 * @param data The data to set.
	 */
	public void setData(String data) {
		this.data = data;
	}

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
}
