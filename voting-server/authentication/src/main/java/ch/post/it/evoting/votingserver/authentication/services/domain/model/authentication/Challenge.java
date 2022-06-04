/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import java.util.Base64;

import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

/**
 * Class representing a server challenge.
 */
public final class Challenge {
	private static final PrimitivesService primitivesService = new PrimitivesService();

	// String representing the server challenge value
	private String challengeValue;

	/**
	 * Builds a new server challenge object containing a random value for a given type and length.
	 *
	 * @param length - byte length of the randomly generated server challenge
	 */
	public Challenge(int length) {
		setChallengeValue(Base64.getEncoder().encodeToString(primitivesService.genRandomBytes(length)));
	}

	/**
	 * Returns the current value of the field challengeValue.
	 *
	 * @return Returns the challengeValue.
	 */
	public String getChallengeValue() {
		return challengeValue;
	}

	// sets the value of the field challengeValue.
	private void setChallengeValue(String challengeValue) {
		this.challengeValue = challengeValue;
	}
}
