/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.authentication;

public class ExtendedAuthenticationUpdate {

	private String oldAuthID;

	private String newAuthID;

	private String newSVK;

	private String authenticationTokenSignature;

	/**
	 * Gets oldAuthID.
	 *
	 * @return Value of oldAuthID.
	 */
	public String getOldAuthID() {
		return oldAuthID;
	}

	/**
	 * Sets new oldAuthID.
	 *
	 * @param oldAuthID New value of oldAuthID.
	 */
	public void setOldAuthID(String oldAuthID) {
		this.oldAuthID = oldAuthID;
	}

	/**
	 * Gets newSVK.
	 *
	 * @return Value of newSVK.
	 */
	public String getNewSVK() {
		return newSVK;
	}

	/**
	 * Sets new newSVK.
	 *
	 * @param newSVK New value of newSVK.
	 */
	public void setNewSVK(String newSVK) {
		this.newSVK = newSVK;
	}

	/**
	 * Gets newAuthID.
	 *
	 * @return Value of newAuthID.
	 */
	public String getNewAuthID() {
		return newAuthID;
	}

	/**
	 * Sets new newAuthID.
	 *
	 * @param newAuthID New value of newAuthID.
	 */
	public void setNewAuthID(String newAuthID) {
		this.newAuthID = newAuthID;
	}

	/**
	 * Gets authenticationTokenSignature.
	 *
	 * @return Value of authenticationTokenSignature.
	 */
	public String getAuthenticationTokenSignature() {
		return authenticationTokenSignature;
	}

	/**
	 * Sets new authenticationTokenSignature.
	 *
	 * @param authenticationTokenSignature New value of authenticationTokenSignature.
	 */
	public void setAuthenticationTokenSignature(String authenticationTokenSignature) {
		this.authenticationTokenSignature = authenticationTokenSignature;
	}
}
