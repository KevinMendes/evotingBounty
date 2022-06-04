/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote;

import javax.ejb.Local;

import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;

/**
 * Provides operations on the vote repository.
 */
@Local
public interface VoteRepository {

	/**
	 * Saves a vote in the repository.
	 *
	 * @param tenantId                      - the tenant id
	 * @param electionEventId               - the election event id
	 * @param vote                          - the vote information with computation proofs and signatures.
	 * @param authenticationTokenJsonString - the authentication token in json format.
	 * @throws ResourceNotFoundException
	 */
	void save(String tenantId, String electionEventId, VoteAndComputeResults vote, String authenticationTokenJsonString)
			throws ResourceNotFoundException;

	/**
	 * Returns the vote as json string identified by tenant, election event, voting card.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @return the vote.
	 * @throws ResourceNotFoundException if the vote is not found.
	 * @throws VoteRepositoryException
	 */
	VoteAndComputeResults findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException, VoteRepositoryException;

	/**
	 * Checks if a vote exists for the specified tenant, electionEvent and votingCard *
	 *
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param votingCardId    - the voting card id.
	 * @return vote exists (true) or not (false).
	 * @throws VoteRepositoryException
	 */
	boolean voteExists(String tenantId, String electionEventId, String votingCardId) throws VoteRepositoryException;
}
