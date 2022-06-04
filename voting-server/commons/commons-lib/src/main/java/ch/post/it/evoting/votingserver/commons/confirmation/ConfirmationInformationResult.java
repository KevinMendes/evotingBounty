/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.confirmation;

import ch.post.it.evoting.domain.election.validation.ValidationError;

/**
 * Result of the confirmation message validation
 */
public class ConfirmationInformationResult {

	// Result of the validation
	private boolean valid;

	// The voting card id
	private String votingCardId;

	// The election event id
	private String electionEventId;

	// Additional info about validations
	private ValidationError validationError;

	/**
	 * Gets electionEventId.
	 *
	 * @return Value of electionEventId.
	 */
	public String getElectionEventId() {
		return electionEventId;
	}

	/**
	 * Sets new electionEventId.
	 *
	 * @param electionEventId New value of electionEventId.
	 */
	public void setElectionEventId(String electionEventId) {
		this.electionEventId = electionEventId;
	}

	/**
	 * Gets valid.
	 *
	 * @return Value of valid.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets new valid.
	 *
	 * @param valid New value of valid.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Gets votingCardId.
	 *
	 * @return Value of votingCardId.
	 */
	public String getVotingCardId() {
		return votingCardId;
	}

	/**
	 * Sets new votingCardId.
	 *
	 * @param votingCardId New value of votingCardId.
	 */
	public void setVotingCardId(String votingCardId) {
		this.votingCardId = votingCardId;
	}

	/**
	 * Returns the current value of the field validationError.
	 *
	 * @return Returns the validationError.
	 */
	public ValidationError getValidationError() {
		return validationError;
	}

	/**
	 * Sets the value of the field validationError.
	 *
	 * @param validationError The validationError to set.
	 */
	public void setValidationError(ValidationError validationError) {
		this.validationError = validationError;
	}

}
