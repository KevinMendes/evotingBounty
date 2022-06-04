/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling ElectionEventContext entities
 */
@Local
public interface ElectionEventContextRepository extends BaseRepository<ElectionEventContextEntity, Integer> {

	/**
	 * Searches for an election event context with the given election event id.
	 *
	 * @param electionEventId the election event id
	 * @return an entity representing the election event context
	 * @throws ResourceNotFoundException if the election event context with the given election event id is not found.
	 */
	ElectionEventContextEntity findByElectionEventId(String electionEventId) throws ResourceNotFoundException;

}