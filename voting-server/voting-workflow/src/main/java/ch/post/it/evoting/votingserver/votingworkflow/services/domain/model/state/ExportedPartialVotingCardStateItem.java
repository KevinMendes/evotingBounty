/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

/**
 * The Class ExportedPartialVotingCardStateItem.
 */
public class ExportedPartialVotingCardStateItem {

	/**
	 * The voting card id.
	 */
	private String votingCardId;

	/**
	 * The state.
	 */
	private String state;

	/**
	 * Gets the voting card id.
	 *
	 * @return the votingCardId
	 */
	public String getVotingCardId() {
		return votingCardId;
	}

	/**
	 * Sets the voting card id.
	 *
	 * @param votingCardId the votingCardId to set
	 */
	public void setVotingCardId(String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * Sets the state.
	 *
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

}
