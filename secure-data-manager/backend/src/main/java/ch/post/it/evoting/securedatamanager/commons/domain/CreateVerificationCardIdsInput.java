/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons.domain;

public class CreateVerificationCardIdsInput {

	private String verificationCardSetId;

	private int numberOfVerificationCardIds;

	private String electionEventId;

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public void setVerificationCardSetId(final String verificationCardSetId) {
		this.verificationCardSetId = verificationCardSetId;
	}

	public int getNumberOfVerificationCardIds() {
		return numberOfVerificationCardIds;
	}

	public void setNumberOfVerificationCardIds(final int numberOfVerificationCardIds) {
		this.numberOfVerificationCardIds = numberOfVerificationCardIds;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public void setElectionEventId(final String electionEventId) {
		this.electionEventId = electionEventId;
	}
}
