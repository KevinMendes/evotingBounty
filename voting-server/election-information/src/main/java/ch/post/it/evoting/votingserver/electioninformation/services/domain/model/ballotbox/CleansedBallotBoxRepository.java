/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox;

import java.util.List;
import java.util.stream.Stream;

import javax.ejb.Local;
import javax.persistence.EntityManager;

import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides operations on the cleansed ballot box repository.
 */
@Local
public interface CleansedBallotBoxRepository extends BaseRepository<CleansedBallotBox, Integer> {

	/**
	 * Returns a stored vote in a cleansed ballot box.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @return the vote stored in the ballot box.
	 * @throws ResourceNotFoundException if the vote is not found.
	 */
	CleansedBallotBox findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;

	/**
	 * Returns a list of clenased ballot boxes for a tenant and election event.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the electionEvent.
	 * @param ballotBoxId     - the identifier of the ballot box.
	 * @return a list of ballot boxes for the given parameters.
	 */
	List<CleansedBallotBox> findByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId);

	/**
	 * Execute persistence actions with a specific entityManager
	 *
	 * @param entityManager to be used
	 */
	void with(EntityManager entityManager);

	/**
	 * @param ballotBoxId the ballot box to count votes from
	 * @return the number of votes in the specified ballot box
	 * @throws CleansedBallotBoxRepositoryException
	 */
	int count(BallotBoxId ballotBoxId) throws CleansedBallotBoxRepositoryException;

	/**
	 * Checks whether the given ballot box exists.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotBoxId     the ballot box id.
	 * @return {@code true} if there is at least one vote for the ballot box, {@code false} otherwise.
	 */
	boolean exists(final String electionEventId, final String ballotBoxId);

	/**
	 * Get a range of encrypted votes from a ballot box, according to the vote set size.`
	 *
	 * @param ballotBoxId a reference to the ballot box with the desired votes
	 * @param offset      starting point of the vote set within the ballot box
	 * @param size        size of the vote set
	 * @return a stream with the requested votes
	 */
	Stream<String> getVoteSet(BallotBoxId ballotBoxId, int offset, int size);
}
