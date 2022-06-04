/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.csv;

public class FailedAuthenticationVotingCardItem {

	private final String credentialId;
	private final String votingCardId;

	public FailedAuthenticationVotingCardItem(final String credentialId, final String votingCardId) {
		this.credentialId = credentialId;
		this.votingCardId = votingCardId;
	}

	String getCredentialId() {
		return credentialId;
	}

	public String getVotingCardId() {
		return votingCardId;
	}
}
