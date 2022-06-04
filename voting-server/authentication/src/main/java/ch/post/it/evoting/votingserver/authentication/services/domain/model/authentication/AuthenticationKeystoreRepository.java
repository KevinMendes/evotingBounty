/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreRepository;

public class AuthenticationKeystoreRepository implements KeystoreRepository {

	@Inject
	private AuthenticationContentService authenticationContentService;

	/**
	 * @see KeystoreRepository#getJsonByTenantEEID(String, String)
	 */
	@Override
	public String getJsonByTenantEEID(final String tenantId, final String electionEventId) throws ResourceNotFoundException {

		final AuthenticationContent authenticationContent = authenticationContentService.getAuthenticationContent(tenantId, electionEventId);

		return authenticationContent.getKeystore().toString();
	}

}
