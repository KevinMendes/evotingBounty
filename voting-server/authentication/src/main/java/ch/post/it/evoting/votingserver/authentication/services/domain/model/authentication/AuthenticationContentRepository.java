/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling AuthenticationContent entities
 */
@Local
public interface AuthenticationContentRepository extends BaseRepository<AuthenticationContent, Integer> {

	/**
	 * Searches for an authentication content with the given tenant, election event.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @return a entity representing the authentication content.
	 */
	AuthenticationContent findByTenantIdElectionEventId(String tenantId, String electionEventId) throws ResourceNotFoundException;

}
