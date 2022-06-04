/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

public class VotingCardStateEvaluator {

	final VotingCardStates state;

	private VotingCardStateEvaluator(final VotingCardStates state) {
		this.state = state;
	}

	public static VotingCardStateEvaluator forState(final VotingCardStates currentState) {
		return new VotingCardStateEvaluator(currentState);
	}

	public boolean votingCardStateRequiresConsistencyCheck() {
		return VotingCardStates.NOT_SENT.equals(this.state) || VotingCardStates.SENT_BUT_NOT_CAST.equals(this.state) || VotingCardStates.CAST
				.equals(this.state);
	}

	/**
	 * Check if the voting card have beed casted
	 *
	 * @return boolean
	 */
	public boolean votingCardDidNotVote() {
		return VotingCardStates.NOT_SENT.equals(this.state);
	}

	/**
	 * Check if the voting card is blocked or not
	 *
	 * @return boolean
	 */
	public boolean votingCardIsBlocked() {
		return VotingCardStates.BLOCKED.equals(this.state);
	}

	/**
	 * Check if the voting card can be blocked or not
	 *
	 * @return
	 */
	public boolean canBeBlocked() {
		return !VotingCardStates.CAST.equals(this.state);
	}
}
