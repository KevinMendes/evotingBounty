/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

import ch.post.it.evoting.domain.election.validation.ValidationErrorType;

/**
 * Authentication Token Validation Exception
 */
public class ExtendedAuthValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final ValidationErrorType errorType;

	public ExtendedAuthValidationException(final ValidationErrorType errorType) {
		this.errorType = errorType;
	}

	public ExtendedAuthValidationException(final ValidationErrorType errorType, Throwable t) {
		super(t);
		this.errorType = errorType;

	}

	public ValidationErrorType getErrorType() {
		return errorType;
	}
}
