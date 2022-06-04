/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.content;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling VerificationContent entities
 */
@Local
public interface VerificationContentRepository extends BaseRepository<VerificationContent, Integer> {

	/**
	 * Searches for a verification content with the given tenant, election event and verification card
	 * set ids.
	 *
	 * @param tenantId              - the identifier of the tenant.
	 * @param electionEventId       - the identifier of the electionEvent.
	 * @param verificationCardSetId - the identifier of the verificationCardSet.
	 * @return a entity representing the verification content.
	 */
	VerificationContent findByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId, String verificationCardSetId)
			throws ResourceNotFoundException;

}
