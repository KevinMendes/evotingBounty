/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballotbox;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * The repository for handling BallotInformation Objects.
 */
@Local
public interface BallotBoxInformationRepository {

	/**
	 * Gets the BallotBox information for a given tenant, election event and ballot box identifier.
	 *
	 * @param tenantId        - identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotBoxId     - identifier of the ballot.
	 * @return a BallotBoxInformationObject.
	 */
	String getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId)
			throws ResourceNotFoundException;

}
