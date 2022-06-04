/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.validation.constraints.NotNull;

/**
 * Data structure used to upload the election event related data.
 */
public class ElectionEventData {

	@NotNull
	private String authenticationContextData;

	@NotNull
	private String authenticationVoterData;

	/**
	 * Returns the current value of the field authenticationContextData.
	 *
	 * @return Returns the authenticationContextData.
	 */
	public String getAuthenticationContextData() {
		return authenticationContextData;
	}

	/**
	 * Sets the value of the field authenticationContextData.
	 *
	 * @param authenticationContextData The authenticationContextData to set.
	 */
	public void setAuthenticationContextData(String authenticationContextData) {
		this.authenticationContextData = authenticationContextData;
	}

	/**
	 * Returns the current value of the field authenticationVoterData.
	 *
	 * @return Returns the authenticationVoterData.
	 */
	public String getAuthenticationVoterData() {
		return authenticationVoterData;
	}

	/**
	 * Sets the value of the field authenticationVoterData.
	 *
	 * @param authenticationVoterData The authenticationVoterData to set.
	 */
	public void setAuthenticationVoterData(String authenticationVoterData) {
		this.authenticationVoterData = authenticationVoterData;
	}

}
