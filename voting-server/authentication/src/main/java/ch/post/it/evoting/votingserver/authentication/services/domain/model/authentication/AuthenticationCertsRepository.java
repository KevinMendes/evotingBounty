/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling AuthenticationCerts entities
 */
@Local
public interface AuthenticationCertsRepository extends BaseRepository<AuthenticationCerts, Integer> {

	/**
	 * Searches for the authentication certs for the given tenant, election event.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @return a entity representing the authentication certs.
	 */
	AuthenticationCerts findByTenantIdElectionEventId(String tenantId, String electionEventId) throws ResourceNotFoundException;

}
