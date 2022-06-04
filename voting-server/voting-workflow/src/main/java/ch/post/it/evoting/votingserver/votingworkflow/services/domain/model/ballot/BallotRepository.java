/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Provides operations on the ballot repository.
 */
@Local
public interface BallotRepository {

	/**
	 * Searches for a ballot identified by tenant, election event and ballot identifier.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a Ballot of the tenant identified by tenant id, and identified by ballot id.
	 * @throws ResourceNotFoundException if ballot is not found.
	 */
	String findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException;
}
