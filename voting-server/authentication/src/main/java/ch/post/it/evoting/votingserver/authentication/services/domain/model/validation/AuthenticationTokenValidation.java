/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.validation;

import java.security.cert.CertificateException;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface to provide functionalities for authentication token validations.
 */
public interface AuthenticationTokenValidation {

	/**
	 * Executes a specific validation.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the token to be validated.
	 * @return true if the validation succeed. Otherwise, false.
	 * @throws ResourceNotFoundException
	 */
	ValidationResult execute(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken)
			throws ResourceNotFoundException, CertificateException;
}
