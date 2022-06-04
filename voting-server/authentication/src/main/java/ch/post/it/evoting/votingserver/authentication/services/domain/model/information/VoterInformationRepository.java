/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.information;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface defining the operations for handling voter information.
 */
@Local
public interface VoterInformationRepository {

	/**
	 * Returns a voter information for a given tenant, election event and credential identifier.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @return The voter information.
	 * @throws ResourceNotFoundException if no voter information is found.
	 */
	VoterInformation findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException;

}
