/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import java.security.cert.CertificateException;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 *
 */
public interface AuthenticationTokenValidationService {

	/**
	 * This method validates an authentication token by applying the set of validation rules.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the token to be validated.
	 * @return an AuthenticationTokenValidationResult containing the result of the validation.
	 */
	ValidationResult validate(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken)
			throws ResourceNotFoundException, CertificateException;

	/**
	 * Returns the authentication content for a given tenant and election event
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @return an authentication content object
	 */
	AuthenticationContent getAuthenticationContent(String tenantId, String electionEventId) throws ResourceNotFoundException;

}
