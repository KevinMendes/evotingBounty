/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state;

import java.util.Optional;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.common.csv.ExportedPartialVotingCardStateItemWriter;

/**
 * Provides operations on the voting card state repository.
 */
public interface VotingCardStateRepository extends BaseRepository<VotingCardState, Long> {

	/**
	 * Returns a voting card state for a given tenant, election event and voting card.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @return The voting card state.
	 * @throws ResourceNotFoundException if voting card state is not found.
	 */
	Optional<VotingCardState> acquire(String tenantId, String electionEventId, String votingCardId);

	/**
	 * Find and write voting cards with inactive state.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param writer          the writer
	 */
	void findAndWriteVotingCardsWithInactiveState(final String tenantId, final String electionEventId,
			final ExportedPartialVotingCardStateItemWriter writer);
}
