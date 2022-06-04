/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence;

import java.util.List;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecBallotBoxStatus;

public interface MixDecBallotBoxStatusRepository {

	void save(final String electionEventId, final String ballotBoxId, final String status, final String errorMessage)
			throws EntryPersistenceException;

	void update(final String electionEventId, final String ballotBoxId, final String status, final String errorMessage)
			throws EntryPersistenceException, ResourceNotFoundException;

	List<MixDecBallotBoxStatus> getMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId) throws ResourceNotFoundException;

	long countByMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId, final String status);

	long countByMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId);

	boolean isVoteSetMixable(final String electionEventId, final String ballotBoxId);

}
