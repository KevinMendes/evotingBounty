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
 * Class containing information of server challenge including challenge, timestamp and signature.
 */
public class ServerChallengeMessage {

	// String representing the challenge value
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.CHALLENGE, groups = SemanticErrorGroup.class)
	private String serverChallenge;

	// String representing the current timestamp
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.TIMESTAMP, groups = SemanticErrorGroup.class)
	private String timestamp;

	// Array of bytes representing the signature of the server challenge
	@NotNull(groups = SyntaxErrorGroup.class)
	private String signature;

	/**
	 * Constructs a new ServerChallengeMessage.
	 */
	public ServerChallengeMessage() {
		super();
	}

	/**
	 * Constructs a new ServerChallengeMessage for the given parameters.
	 *
	 * @param serverChallenge - the server challenge.
	 * @param timestamp       - the timestamp of the server
	 * @param signature       - the signature of the challenge
	 */
	public ServerChallengeMessage(String serverChallenge, String timestamp, String signature) {
		this.serverChallenge = serverChallenge;
		this.timestamp = timestamp;
		this.signature = signature;
	}

	/**
	 * Returns the current value of the field signature.
	 *
	 * @return Returns the signature
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

	/**
	 * Returns the current value of the field serverChallenge.
	 *
	 * @return Returns the serverChallenge.
	 */
	public String getServerChallenge() {
		return serverChallenge;
	}

	/**
	 * Sets the value of the field serverChallenge.
	 *
	 * @param serverChallenge The serverChallenge to set.
	 */
	public void setServerChallenge(String serverChallenge) {
		this.serverChallenge = serverChallenge;
	}

	/**
	 * Returns the current value of the field timestamp.
	 *
	 * @return Returns the timestamp.
	 */
	public String getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the value of the field timestamp.
	 *
	 * @param timestamp The timestamp to set.
	 */
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
