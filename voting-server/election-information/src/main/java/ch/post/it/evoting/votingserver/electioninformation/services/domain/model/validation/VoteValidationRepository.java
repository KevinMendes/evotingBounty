/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for storing vote validation result.
 */
@Local
public interface VoteValidationRepository extends BaseRepository<VoteValidation, Integer> {

	/**
	 * Returns the result of finding the vote validation by the input parameters.
	 *
	 * @param tenantId        the tenant id.
	 * @param electionEventId the election event id.
	 * @param votingCardId    the voting card id.
	 * @param voteHash        the hash of the vote.
	 * @return the vote validation if found.
	 * @throws ResourceNotFoundException
	 */
	VoteValidation findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId, String voteHash)
			throws ResourceNotFoundException;
}
