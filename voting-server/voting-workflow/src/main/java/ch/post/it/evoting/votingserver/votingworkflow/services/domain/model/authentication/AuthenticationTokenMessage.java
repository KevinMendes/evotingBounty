/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import ch.post.it.evoting.domain.election.validation.ValidationError;

/**
 * Class representing an authentication token and a validation error.
 */
public class AuthenticationTokenMessage {

	private AuthenticationToken authenticationToken;

	private ValidationError validationError;

	/**
	 * Returns the current value of the field authenticationToken.
	 *
	 * @return Returns the authenticationToken.
	 */
	public AuthenticationToken getAuthenticationToken() {
		return authenticationToken;
	}

	/**
	 * Sets the value of the field authenticationToken.
	 *
	 * @param authenticationToken The authenticationToken to set.
	 */
	public void setAuthenticationToken(AuthenticationToken authenticationToken) {
		this.authenticationToken = authenticationToken;
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
