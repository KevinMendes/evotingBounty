/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard;

public class VotingCard {

	private final String votingCardId;

	public VotingCard(final String votingCardId) {
		super();
		this.votingCardId = votingCardId;
	}

	public String getVotingCardId() {
		return votingCardId;
	}

}
