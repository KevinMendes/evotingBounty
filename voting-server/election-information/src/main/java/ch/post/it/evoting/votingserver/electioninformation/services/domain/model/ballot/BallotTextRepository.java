/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides extra operations on the ballot text repository.
 */
@Local
public interface BallotTextRepository extends BaseRepository<BallotText, Integer> {

	/**
	 * Searches for a ballot texts with the given ballotId, election event and tenant.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a entity representing the ballot text.
	 * @throws ResourceNotFoundException if ballot text is not found.
	 */
	BallotText findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException;
}
