/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification;

import java.io.IOException;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Repository for handling Verification data.
 */
@Local
public interface VerificationRepository {

	/**
	 * Searches for the verification data related to given parameters.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the election event.
	 * @param verificationCardId - the identifier of the verification card.
	 * @return The associated verification data, if found.
	 * @throws ResourceNotFoundException if the verification is not found.
	 */
	Verification findByTenantElectionEventVotingCard(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException, IOException;
}
