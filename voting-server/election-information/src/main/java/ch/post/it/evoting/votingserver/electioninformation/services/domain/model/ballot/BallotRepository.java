/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides extra operations on the ballot repository.
 */
@Local
public interface BallotRepository extends BaseRepository<Ballot, Integer> {

	/**
	 * Searches for a ballot with the given id and tenant.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the external identifier of the ballot.
	 * @return a entity representing the ballot.
	 * @throws ResourceNotFoundException if ballot is not found.
	 */
	Ballot findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException;
}
