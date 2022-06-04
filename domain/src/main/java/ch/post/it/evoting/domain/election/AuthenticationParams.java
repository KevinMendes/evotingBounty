/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election;

import java.util.Properties;

/**
 * Bean with some properties to be added inside the {@link AuthenticationContextData} object.
 */
public class AuthenticationParams {

	private String challengeResExpTime;

	private String authTokenExpTime;

	private String challengeLength;

	/**
	 *
	 */
	public AuthenticationParams() {
	}

	/**
	 * @param challengeResExpTime
	 * @param authTokenExpTime
	 * @param challengeLength
	 */
	public AuthenticationParams(final String challengeResExpTime, final String authTokenExpTime, final String challengeLength) {
		super();
		this.challengeResExpTime = challengeResExpTime;
		this.authTokenExpTime = authTokenExpTime;
		this.challengeLength = challengeLength;
	}

	/**
	 * @return Returns the challengeResExpTime.
	 */
	public String getChallengeResExpTime() {
		return challengeResExpTime;
	}

	/**
	 * @param challengeResExpTime The challengeResExpTime to set.
	 */
	public void setChallengeResExpTime(final String challengeResExpTime) {
		this.challengeResExpTime = challengeResExpTime;
	}

	/**
	 * @return Returns the authTokenExpTime.
	 */
	public String getAuthTokenExpTime() {
		return authTokenExpTime;
	}

	/**
	 * @param authTokenExpTime The authTokenExpTime to set.
	 */
	public void setAuthTokenExpTime(final String authTokenExpTime) {
		this.authTokenExpTime = authTokenExpTime;
	}

	/**
	 * @return Returns the challengeLength.
	 */
	public String getChallengeLength() {
		return challengeLength;
	}

	/**
	 * @param challengeLength The challengeLength to set.
	 */
	public void setChallengeLength(final String challengeLength) {
		this.challengeLength = challengeLength;
	}

	public void setFromProperties(final Properties properties) {

		challengeResExpTime = (String) properties.get("challengeResExpTime");

		authTokenExpTime = (String) properties.get("authTokenExpTime");

		challengeLength = (String) properties.get("challengeLength");
	}
}
