/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.credential;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides operations on the credential repository.
 */
@Local
public interface CredentialRepository extends BaseRepository<Credential, Integer> {

	/**
	 * Returns a credential data for a given tenant, election event and credential identifier.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @return The credential data.
	 * @throws ResourceNotFoundException if credential data is not found.
	 */
	Credential findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException;

	/**
	 * Returns whether a credential data exists for given tenant, election event and credential
	 * identifier.
	 *
	 * @param tenantId        - the tenant identifier
	 * @param electionEventId - the election event identifier
	 * @param credentialId    - the credential identifier
	 * @return the credential data exists.
	 */
	boolean hasWithTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId);
}
