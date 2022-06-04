/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote;

import ch.post.it.evoting.domain.election.validation.ValidationError;

/**
 * Class with the value of the vote validation generated when a ballot is stored.
 */
public class ValidationVoteResult {

	// True if the vote is valid. Otherwise, false.
	private boolean valid;

	// Details of validation vote result.
	private ValidationError validationError;

	// Choice codes
	private String choiceCodes;

	/**
	 * Returns the current value of the field valid.
	 *
	 * @return Returns the valid.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets the value of the field valid.
	 *
	 * @param valid The valid to set.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Returns the current value of the field choiceCodes.
	 *
	 * @return Returns the choiceCodes.
	 */
	public String getChoiceCodes() {
		return choiceCodes;
	}

	/**
	 * Sets the value of the field choiceCodes.
	 *
	 * @param choiceCodes The choiceCodes to set.
	 */
	public void setChoiceCodes(String choiceCodes) {
		this.choiceCodes = choiceCodes;
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
