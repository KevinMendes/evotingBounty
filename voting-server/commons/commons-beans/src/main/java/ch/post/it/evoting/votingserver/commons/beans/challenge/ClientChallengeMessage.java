/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.challenge;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.domain.election.model.constants.Patterns;

/**
 * Class representing the client challenge used for authentication purposes.
 */
public class ClientChallengeMessage {

	// String representing the challenge value
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.CHALLENGE, groups = SemanticErrorGroup.class)
	private String clientChallenge;

	// Array of bytes representing the signature of the server challenge
	@NotNull(groups = SyntaxErrorGroup.class)
	private String signature;

	/**
	 * Returns the current value of the field clientChallenge.
	 *
	 * @return Returns the clientChallenge.
	 */
	public String getClientChallenge() {
		return clientChallenge;
	}

	/**
	 * Sets the value of the field clientChallenge.
	 *
	 * @param clientChallenge The clientChallenge to set.
	 */
	public void setClientChallenge(String clientChallenge) {
		this.clientChallenge = clientChallenge;
	}

	/**
	 * Returns the current value of the field signature.
	 *
	 * @return Returns the signature.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * Sets the value of the field signature.
	 *
	 * @param signature The signature to set.
	 */
	public void setSignature(String signature) {
		this.signature = signature;
	}

}
