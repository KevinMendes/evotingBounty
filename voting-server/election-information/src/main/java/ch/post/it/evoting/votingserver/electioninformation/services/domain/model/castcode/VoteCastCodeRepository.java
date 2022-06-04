/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard.VotingCardWriter;

/**
 * Provides extra operations on the vote cast code repository.
 */
@Local
public interface VoteCastCodeRepository extends BaseRepository<VoteCastCode, Integer> {

	/**
	 * Reads and returns a VoteCastCode for the given tenant election event and voting card id.
	 *
	 * @param tenantId        - the identifier of the tenant id.
	 * @param electionEventId - the identifier of the election event id.
	 * @param votingCardId    - the voting card id.
	 * @return VoteCastCode objects for the given tenant, election event and voting card id.
	 * @throws ResourceNotFoundException if the vote cast code data can not be found.
	 */
	VoteCastCode findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;

	/**
	 * Stores a vote cast code.
	 *
	 * @param tenantId        - the identifier of the tenant id.
	 * @param electionEventId - the identifier of the election event id.
	 * @param votingCardId    - the voting card id.
	 * @param voteCastCode    - the vote cast code.
	 * @throws DuplicateEntryException if the object exists for the given tenant, election event and voting card.
	 */
	void save(String tenantId, String electionEventId, String votingCardId, VoteCastCode voteCastCode) throws DuplicateEntryException;

	/**
	 * Find and write voting cards that had been casted.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param writer          the writer
	 */
	void findAndWriteCastVotingCards(String tenantId, String electionEventId, VotingCardWriter writer);
}
