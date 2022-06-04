/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

import java.util.List;

public class CertificateValidationResult {

	private boolean valid;

	private List<String> validationErrorMessages;

	/**
	 * Gets isValidated.
	 *
	 * @return Value of isValidated.
	 */
	public boolean isValid() {
		return valid;
	}

	/**
	 * Sets new isValidated.
	 *
	 * @param valid New value of isValidated.
	 */
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	/**
	 * Gets validationErrorMessages.
	 *
	 * @return Value of validationErrorMessages.
	 */
	public List<String> getValidationErrorMessages() {
		return validationErrorMessages;
	}

	/**
	 * Sets new validationErrorMessages.
	 *
	 * @param validationErrorMessages New value of validationErrorMessages.
	 */
	public void setValidationErrorMessages(List<String> validationErrorMessages) {
		this.validationErrorMessages = validationErrorMessages;
	}
}
