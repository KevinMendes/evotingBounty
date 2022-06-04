/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;

public interface AuthenticationTokenFactory {

	/**
	 * Builds an authentication token.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @return an authentication token initialized according with the parameters.
	 * @throws AuthenticationTokenGenerationException
	 * @throws AuthenticationTokenSigningException
	 */
	AuthenticationTokenMessage buildAuthenticationToken(String tenantId, String electionEventId, String credentialId)
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException;

}
