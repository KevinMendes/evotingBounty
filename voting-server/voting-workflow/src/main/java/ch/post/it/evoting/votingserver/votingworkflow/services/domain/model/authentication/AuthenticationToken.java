/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;

/**
 * Class representing an authentication token.
 */
public class AuthenticationToken {

	// String representing the authentication token id value
	@NotNull(groups = SyntaxErrorGroup.class)
	private String id;

	// the voter information
	@NotNull(groups = SyntaxErrorGroup.class)
	@Valid
	private VoterInformation voterInformation;

	// String representing the current timestamp
	@NotNull(groups = SyntaxErrorGroup.class)
	private String timestamp;

	// String representing the authentication token signature
	@NotNull(groups = SyntaxErrorGroup.class)
	private String signature;

	/**
	 * Constructs a new AuthenticationToken for the given parameters.
	 *
	 * @param voterInformation            - the voter information for a given tenant, election event and voting
	 *                                    card.
	 * @param base64AuthenticationTokenId - the string in base64
	 * @param currentTimestamp            - the current timestamp.
	 * @param signature                   - the signature of the authentication token.
	 */
	public AuthenticationToken(VoterInformation voterInformation, String base64AuthenticationTokenId, String currentTimestamp, String signature) {
		this.voterInformation = voterInformation;
		this.id = base64AuthenticationTokenId;
		this.timestamp = currentTimestamp;
		this.signature = signature;
	}

	/**
	 * Empty constructor.
	 */
	public AuthenticationToken() {
	}

	/**
	 * Returns the current value of the field voterInformation.
	 *
	 * @return Returns the voterInformation.
	 */
	public VoterInformation getVoterInformation() {
		return voterInformation;
	}

	/**
	 * Sets the value of the field voterInformation.
	 *
	 * @param voterInformation The voterInformation to set.
	 */
	public void setVoterInformation(VoterInformation voterInformation) {
		this.voterInformation = voterInformation;
	}

	/**
	 * Returns the current value of the field authentication token id.
	 *
	 * @return Returns the authentication token id.
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
