/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.validation;

/**
 * Entity holding the result of a validation.
 */
public class ValidationResult {

	/**
	 * The final result of the validation. If no validation failed, then the result is true. If there are rules that failed, result is false.
	 */
	private boolean result;

	/**
	 * The validation result code with description of the error or parameters if corresponds.
	 */
	private ValidationError validationError;

	public ValidationResult(final boolean result) {
		this.result = result;
		if (result) {
			this.validationError = new ValidationError(ValidationErrorType.SUCCESS);
		}
	}

	public ValidationResult() {
	}

	/**
	 * Returns the current value of the field result.
	 *
	 * @return Returns the result.
	 */
	public boolean isResult() {
		return result;
	}

	/**
	 * Sets the value of the field result.
	 *
	 * @param result The result to set.
	 */
	public void setResult(final boolean result) {
		this.result = result;
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
	public void setValidationError(final ValidationError validationError) {
		this.validationError = validationError;
	}

}
