/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

/**
 * Decorator of the vote repository.
 */
@Decorator
public abstract class VoteRepositoryDecorator implements VoteRepository {

	@Inject
	@Delegate
	private VoteRepository voteRepository;

	@Override
	public void save(String tenantId, String electionEventId, VoteAndComputeResults vote, String authenticationTokenJsonString)
			throws ResourceNotFoundException {
		voteRepository.save(tenantId, electionEventId, vote, authenticationTokenJsonString);
	}

	@Override
	public VoteAndComputeResults findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException, VoteRepositoryException {
		return voteRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
	}

	@Override
	public boolean voteExists(final String tenantId, final String electionEventId, final String votingCardId) throws VoteRepositoryException {
		return voteRepository.voteExists(tenantId, electionEventId, votingCardId);
	}
}
