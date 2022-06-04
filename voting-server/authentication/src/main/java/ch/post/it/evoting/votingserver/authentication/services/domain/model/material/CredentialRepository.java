/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.material;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Provides operations on the credential repository.
 */
@Local
public interface CredentialRepository {

	/**
	 * Searches for credential data identified by the given tenant identifier, voting card identifier
	 * and election event identifier.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param credentialId    - the identifier of the credential.
	 * @return a Credential object containing credential data of a voter.
	 * @throws ResourceNotFoundException if no voter material is found.
	 */
	Credential findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException;
}
