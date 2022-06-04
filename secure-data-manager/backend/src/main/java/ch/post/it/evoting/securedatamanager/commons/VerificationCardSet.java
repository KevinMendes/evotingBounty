/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons;

/**
 * Verification card set containing the election event identifier, the ballot box identifier, the voting card set identifier, the verification card
 * set identifier and the administration board identifier
 */
public class VerificationCardSet {
	private final String adminBoardId;
	private final String ballotBoxId;
	private final String electionEventId;
	private final String verificationCardSetId;
	private final String votingCardSetId;

	public VerificationCardSet(final String electionEventId, final String ballotBoxId, final String votingCardSetId, final String verificationCardSetId,
			final String adminBoardId) {
		this.adminBoardId = adminBoardId;
		this.ballotBoxId = ballotBoxId;
		this.electionEventId = electionEventId;
		this.verificationCardSetId = verificationCardSetId;
		this.votingCardSetId = votingCardSetId;
	}

	public String getAdminBoardId() {
		return adminBoardId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public String getVotingCardSetId() {
		return votingCardSetId;
	}

}
