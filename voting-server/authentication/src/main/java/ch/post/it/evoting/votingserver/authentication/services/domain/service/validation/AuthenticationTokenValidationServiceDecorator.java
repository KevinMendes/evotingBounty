/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import java.security.cert.CertificateException;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Decorator for authentication token validation service.
 */
@Decorator
public class AuthenticationTokenValidationServiceDecorator implements AuthenticationTokenValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenValidationServiceDecorator.class);

	@Inject
	@Delegate
	private AuthenticationTokenValidationService authenticationTokenValidationService;

	/**
	 * @see AuthenticationTokenValidationService#validate(String, String, String, AuthenticationToken)
	 */
	@Override
	public ValidationResult validate(final String tenantId, final String electionEventId, final String votingCardId,
			final AuthenticationToken authenticationToken)
			throws ResourceNotFoundException, CertificateException {
		LOGGER.info("Starting validation of Authentication Token.");
		try {
			final ValidationResult validationResult = authenticationTokenValidationService
					.validate(tenantId, electionEventId, votingCardId, authenticationToken);
			LOGGER.info("Result of Authentication Token validation. [result {}]", validationResult.isResult());
			return validationResult;
		} catch (final ResourceNotFoundException | CertificateException e) {
			LOGGER.info("Result of Authentication Token validation. [result {}]", false);
			throw e;
		}
	}

	/**
	 * @see AuthenticationTokenValidationService#getAuthenticationContent(String, String)
	 */
	@Override
	public AuthenticationContent getAuthenticationContent(final String tenantId, final String electionEventId) throws ResourceNotFoundException {
		return authenticationTokenValidationService.getAuthenticationContent(tenantId, electionEventId);
	}
}
