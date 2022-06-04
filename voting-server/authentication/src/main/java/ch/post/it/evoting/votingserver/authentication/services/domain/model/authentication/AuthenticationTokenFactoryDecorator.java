/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;

/**
 * Authentication token factory decorator.
 */
@Decorator
public class AuthenticationTokenFactoryDecorator implements AuthenticationTokenFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenFactoryDecorator.class);
	@Inject
	@Delegate
	private AuthenticationTokenFactory authenticationTokenFactory;

	/**
	 * @see AuthenticationTokenFactory#buildAuthenticationToken(String, String, String)
	 */
	@Override
	public AuthenticationTokenMessage buildAuthenticationToken(final String tenantId, final String electionEventId, final String votingCardId)
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		// generate token
		final AuthenticationTokenMessage authenticationTokenMessage = authenticationTokenFactory
				.buildAuthenticationToken(tenantId, electionEventId, votingCardId);

		final ValidationError validationError = authenticationTokenMessage.getValidationError();
		if (validationError != null) {
			switch (validationError.getValidationErrorType()) {
			case ELECTION_NOT_STARTED:
			case ELECTION_OVER_DATE:
			case SUCCESS:
				break;
			default:
				LOGGER.error("Unknown validation error type: {}", validationError.getValidationErrorType());
				break;
			}
		}
		return authenticationTokenMessage;
	}
}
