/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.AuthenticationTokenValidation;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * This class implements the expiration time validation of the authentication token.
 */
public class AuthenticationTokenExpirationTimeValidation implements AuthenticationTokenValidation {

	private static final Long CONSTANT_ZERO = 0L;

	private static final Long MILLIS = 1000L;

	@Inject
	AuthenticationContentService authenticationContentService;

	/**
	 * This method implements the validation of token expiration time. That is, the difference between
	 * the current time and the token timestamp is less or equal than the (previously configured)
	 * token expiration time.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the authentication token to be validated.
	 * @return validationResult, if successfully validated. Otherwise, a exception is thrown.
	 * @throws ResourceNotFoundException if the authentication content is not found.
	 */
	@Override
	public ValidationResult execute(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken)
			throws ResourceNotFoundException {

		ValidationResult validationResult = new ValidationResult(true);
		// get current time
		long currentTimestamp = System.currentTimeMillis();

		long tokenTimestamp = getTokenTimestamp(authenticationToken);

		long expirationTime = getExpirationTime(tenantId, electionEventId);

		if (isNegative(tokenTimestamp)) {
			throw new AuthTokenValidationException(ValidationErrorType.AUTH_TOKEN_EXPIRED, expirationTimeStr(expirationTime));
		}

		final long delta = currentTimestamp - tokenTimestamp;
		if (isNegative(delta)) {
			throw new AuthTokenValidationException(ValidationErrorType.AUTH_TOKEN_EXPIRED, expirationTimeStr(expirationTime));
		}

		if (!isStillValid(delta, expirationTime)) {
			throw new AuthTokenValidationException(ValidationErrorType.AUTH_TOKEN_EXPIRED, expirationTimeStr(expirationTime));
		}
		return validationResult;
	}

	private boolean isStillValid(long delta, long tokenExpirationConfiguration) {
		return delta <= tokenExpirationConfiguration;
	}

	private long getExpirationTime(String tenantId, String electionEventId) throws ResourceNotFoundException {
		AuthenticationContent authenticationContent = authenticationContentService.getAuthenticationContent(tenantId, electionEventId);
		return authenticationContent.getTokenExpirationTime() * MILLIS;
	}

	private boolean isNegative(long value) {
		return value < CONSTANT_ZERO;
	}

	private Long getTokenTimestamp(AuthenticationToken authenticationToken) {
		return Long.valueOf(authenticationToken.getTimestamp());
	}

	private String expirationTimeStr(long expirationTime) {
		return String.format("Expiration time, ms: %s", expirationTime);
	}
}
