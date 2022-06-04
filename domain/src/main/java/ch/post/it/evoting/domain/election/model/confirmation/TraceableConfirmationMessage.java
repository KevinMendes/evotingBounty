/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.confirmation;

/**
 * Extends the confirmation message with the authentication token signature and the voting card ID.
 */
public class TraceableConfirmationMessage extends ConfirmationMessage {

	private String authenticationTokenSignature;

	private String votingCardId;

	public String getAuthenticationTokenSignature() {
		return authenticationTokenSignature;
	}

	public void setAuthenticationTokenSignature(String authenticationTokenSignature) {
		this.authenticationTokenSignature = authenticationTokenSignature;
	}

	public String getVotingCardId() {
		return votingCardId;
	}

	public void setVotingCardId(String votingCardId) {
		this.votingCardId = votingCardId;
	}

}
