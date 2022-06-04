/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.content;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling CodesMapping entities
 */
@Local
public interface CodesMappingRepository extends BaseRepository<CodesMapping, Integer> {

	/**
	 * Searches for a codes mapping with the given tenant, election event and verification card ids.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the electionEvent.
	 * @param verificationCardId - the identifier of the verificationCard.
	 * @return a entity representing the codes mapping.
	 */
	CodesMapping findByTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException;

	/**
	 * Returns whether exists a codes mapping with the specified tenant identifier, election event
	 * identifier and verification card identifier.
	 *
	 * @param tenantId           - the identifier of the tenant
	 * @param electionEventId    - the identifier of the electionEvent
	 * @param verificationCardId - the identifier of the verificationCard
	 * @return the mapping exists
	 */
	boolean hasWithTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId);
}
