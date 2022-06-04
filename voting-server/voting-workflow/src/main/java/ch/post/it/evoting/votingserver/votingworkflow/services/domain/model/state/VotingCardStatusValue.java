/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

/**
 * The Class VotingCardStatusValue.
 */
public class VotingCardStatusValue {

	/**
	 * The voting card id.
	 */
	private String votingCardId;

	/**
	 * The status.
	 */
	private String status;

	/**
	 * The Confirmation Attempts
	 */
	private Long confirmationAttempts;

	/**
	 * Gets the voting card id.
	 *
	 * @return Returns the votingCardId.
	 */
	public String getVotingCardId() {
		return votingCardId;
	}

	/**
	 * Sets the voting card id.
	 *
	 * @param votingCardId The votingCardId to set.
	 */
	public void setVotingCardId(final String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Gets the status.
	 *
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status The status to set.
	 */
	public void setStatus(final String status) {
		this.status = status;
	}

	/**
	 * Returns the current value of the field confirmationAttempts.
	 *
	 * @return Returns the confirmationAttempts.
	 */
	public Long getConfirmationAttempts() {
		return confirmationAttempts;
	}

	/**
	 * Sets the value of the field confirmationAttempts.
	 *
	 * @param confirmationAttempts The confirmationAttempts to set.
	 */
	public void setConfirmationAttempts(Long confirmationAttempts) {
		this.confirmationAttempts = confirmationAttempts;
	}
}
