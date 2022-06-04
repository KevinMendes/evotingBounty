/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.authentication;

import javax.json.JsonObject;

/**
 * Class which represents the authentication contents for a specific election event and tenant
 */
public class AuthenticationContent {

	JsonObject keystore;

	Integer challengeExpirationTime;

	Integer tokenExpirationTime;

	Integer challengeLength;

	String password;

	/**
	 * gets the value of the field key store
	 *
	 * @return
	 */
	public JsonObject getKeystore() {
		return keystore;
	}

	/**
	 * sets the value of the field keystore.
	 *
	 * @param keystore
	 */
	public void setKeystore(final JsonObject keystore) {
		this.keystore = keystore;
	}

	/**
	 * gets the value of the field challenge expiration time
	 *
	 * @return
	 */
	public Integer getChallengeExpirationTime() {
		return challengeExpirationTime;
	}

	/**
	 * sets the value of the field challengeExpirationTime.
	 *
	 * @param challengeExpirationTime
	 */
	public void setChallengeExpirationTime(final Integer challengeExpirationTime) {
		this.challengeExpirationTime = challengeExpirationTime;
	}

	/**
	 * gets the value of the field tokenExpirationTime
	 *
	 * @return
	 */
	public Integer getTokenExpirationTime() {
		return tokenExpirationTime;
	}

	/**
	 * sets the value of the field tokenExpirationTime.
	 *
	 * @param tokenExpirationTime
	 */
	public void setTokenExpirationTime(final Integer tokenExpirationTime) {
		this.tokenExpirationTime = tokenExpirationTime;
	}

	/**
	 * gets the value of the field challenge length.
	 *
	 * @return
	 */
	public Integer getChallengeLength() {
		return challengeLength;
	}

	/**
	 * sets the value of the field challengeLength.
	 *
	 * @param challengeLength
	 */
	public void setChallengeLength(final Integer challengeLength) {
		this.challengeLength = challengeLength;
	}

	/**
	 * Gets the password value
	 *
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password value
	 *
	 * @param password
	 */
	public void setPassword(final String password) {
		this.password = password;
	}
}
