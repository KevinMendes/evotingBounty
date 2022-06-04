/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.verification;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides operations on the verification repository.
 */
@Local
public interface VerificationRepository extends BaseRepository<Verification, Integer> {

	/**
	 * Returns a verification data for a given tenant, election event and verification card.
	 *
	 * @param tenantId           - the tenant identifier.
	 * @param electionEventId    - the election event identifier.
	 * @param verificationCardId - the verification card identifier.
	 * @return The verification data.
	 * @throws ResourceNotFoundException if the verification data is not found.
	 */
	Verification findByTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException;

	/**
	 * Returns whether exists a verification data for given tenant, election event and verification
	 * card.
	 *
	 * @param tenantId           - the tenant identifier
	 * @param electionEventId    - the election event identifier
	 * @param verificationCardId - the verification card identifier
	 * @return the data exists.
	 */
	boolean hasWithTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId);
}
