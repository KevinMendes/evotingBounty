/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation;

import javax.ejb.Local;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenRepositoryException;

/**
 * Service for validating the authentication token
 */
@Local
public interface AuthenticationTokenService {

	/**
	 * Validates an auntenticationToken
	 *
	 * @param tenantId
	 * @param electionEventId
	 * @param token
	 * @return
	 * @throws AuthTokenRepositoryException
	 */
	ValidationResult validateToken(String tenantId, String electionEventId, AuthenticationToken token) throws AuthTokenRepositoryException;
}
