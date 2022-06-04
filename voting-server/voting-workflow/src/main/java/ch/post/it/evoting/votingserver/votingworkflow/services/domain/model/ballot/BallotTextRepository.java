/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Provides operations on the ballot text repository.
 */
@Local
public interface BallotTextRepository {

	/**
	 * Searches for a ballot text of a ballot identified by ballot, tenant and election event
	 * identifiers.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a ballot text in json format.
	 * @throws ResourceNotFoundException if ballot text is not found.
	 */
	String findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException;
}
