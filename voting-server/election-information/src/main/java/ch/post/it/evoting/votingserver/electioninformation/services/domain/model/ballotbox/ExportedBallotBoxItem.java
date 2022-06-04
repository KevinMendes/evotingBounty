/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

public class ExportedBallotBoxItem {

	private final String tenantId;

	private final String electionEventId;

	private final String votingCardId;

	private final String ballotId;

	private final String ballotBoxId;

	private final String vote;

	private final String voteComputationResults;

	private final String voteCastCode;

	private final String castCodeComputationResults;

	private ExportedBallotBoxItem(final ExportedBallotBoxItemBuilder builder) {
		this.tenantId = builder.tenantId;
		this.electionEventId = builder.electionEventId;
		this.votingCardId = builder.votingCardId;
		this.ballotId = builder.ballotId;
		this.ballotBoxId = builder.ballotBoxId;
		this.vote = builder.vote;
		this.voteComputationResults = builder.voteComputationResults;
		this.voteCastCode = builder.voteCastCode;
		this.castCodeComputationResults = builder.castCodeComputationResults;
	}

	public String getTenantId() {
		return tenantId;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVotingCardId() {
		return votingCardId;
	}

	public String getBallotId() {
		return ballotId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public String getVote() {
		return vote;
	}

	public String getVoteCastCode() {
		return voteCastCode;
	}

	public String getCastCodeComputationResults() {
		return castCodeComputationResults;
	}

	public String getVoteComputationResults() {
		return voteComputationResults;
	}

	public static class ExportedBallotBoxItemBuilder {

		private String tenantId;

		private String electionEventId;

		private String votingCardId;

		private String ballotId;

		private String ballotBoxId;

		private String vote;

		private String voteComputationResults;

		private String voteCastCode;

		private String castCodeComputationResults;

		public ExportedBallotBoxItemBuilder setTenantId(final String tenantId) {
			this.tenantId = tenantId;
			return this;
		}

		public ExportedBallotBoxItemBuilder setElectionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		public ExportedBallotBoxItemBuilder setVotingCardId(final String votingCardId) {
			this.votingCardId = votingCardId;
			return this;
		}

		public ExportedBallotBoxItemBuilder setBallotId(final String ballotId) {
			this.ballotId = ballotId;
			return this;
		}

		public ExportedBallotBoxItemBuilder setBallotBoxId(final String ballotBoxId) {
			this.ballotBoxId = ballotBoxId;
			return this;
		}

		public ExportedBallotBoxItemBuilder setVote(final String vote) {
			this.vote = vote;
			return this;
		}

		public ExportedBallotBoxItemBuilder setVoteCastCode(final String voteCastCode) {
			this.voteCastCode = voteCastCode;
			return this;
		}

		public ExportedBallotBoxItemBuilder setCastCodeComputationResults(final String castCodeComputationResults) {
			this.castCodeComputationResults = castCodeComputationResults;
			return this;
		}

		public ExportedBallotBoxItemBuilder setVoteComputationResults(final String voteComputationResults) {
			this.voteComputationResults = voteComputationResults;
			return this;
		}

		public ExportedBallotBoxItem build() {
			return new ExportedBallotBoxItem(this);
		}

	}

}
