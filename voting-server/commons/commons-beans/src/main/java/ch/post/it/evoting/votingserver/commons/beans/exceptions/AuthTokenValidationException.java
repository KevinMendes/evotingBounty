/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

import ch.post.it.evoting.domain.election.validation.ValidationErrorType;

/**
 * Authentication Token Validation Exception
 */
public class AuthTokenValidationException extends RuntimeException {

	private static final long serialVersionUID = 3045584269314820919L;

	private final ValidationErrorType errorType;

	public AuthTokenValidationException(final ValidationErrorType errorType) {
		this.errorType = errorType;
	}

	public AuthTokenValidationException(final ValidationErrorType errorType, final String message) {
		super(message);
		this.errorType = errorType;
	}

	public AuthTokenValidationException(final ValidationErrorType errorType, final Throwable throwable) {
		super(throwable);
		this.errorType = errorType;
	}

	public ValidationErrorType getErrorType() {
		return errorType;
	}
}
