/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import ch.post.it.evoting.domain.election.errors.SemanticErrorGroup;
import ch.post.it.evoting.domain.election.errors.SyntaxErrorGroup;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.domain.model.Constants;
import ch.post.it.evoting.votingserver.commons.domain.model.Patterns;

/**
 * Class representing an authentication token.
 */
public class AuthenticationToken {

	// String representing the authentication token id value
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.CHALLENGE, groups = SemanticErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SyntaxErrorGroup.class)
	private String id;

	// the voter information
	@NotNull(groups = SyntaxErrorGroup.class)
	@Valid
	private VoterInformation voterInformation;

	// String representing the current timestamp
	@NotNull(groups = SyntaxErrorGroup.class)
	@Pattern(regexp = Patterns.TIMESTAMP, groups = SemanticErrorGroup.class)
	@Size(max = Constants.COLUMN_LENGTH_100, groups = SyntaxErrorGroup.class)
	private String timestamp;

	// Array of bytes representing the signature of the token data
	@NotNull(groups = SyntaxErrorGroup.class)
	private String signature;

	/**
	 * Constructs a new AuthenticationToken for the given parameters.
	 *
	 * @param voterInformation            - the voter information for a given tenant, election event and voting
	 *                                    card.
	 * @param base64AuthenticationTokenId - the string in base64
	 * @param currentTimestamp            - the current timestamp.
	 * @param signature                   - the signature of the auth token
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
		// empty constructor
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
	 * Returns the current value of the field authentication token id.
	 *
	 * @return Returns the authentication token id.
	 */
	public String getId() {
		return id;
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
	 * Returns the current value of the field signature.
	 *
	 * @return Returns the signature.
	 */
	public String getSignature() {
		return signature;
	}

}
