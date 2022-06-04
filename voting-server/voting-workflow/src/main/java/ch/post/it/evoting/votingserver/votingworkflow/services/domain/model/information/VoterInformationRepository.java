/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * The Interface VoterInformationRepository.
 */
public interface VoterInformationRepository {

	/**
	 * Gets the by tenant id election event id voting card id.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return the by tenant id election event id voting card id
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	VoterInformation getByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;
}
