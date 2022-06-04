/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCode;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.castcode.VoteCastCodeRepository;

/**
 * Decorator of the vote cast code repository.
 */
@Decorator
public abstract class VoteCastCodeRepositoryDecorator implements VoteCastCodeRepository {

	@Inject
	@Delegate
	private VoteCastCodeRepository voteCastCodeRepository;

	/**
	 * @see VoteCastCodeRepository#findByTenantIdElectionEventIdVotingCardId(String, String, String)
	 */
	@Override
	public VoteCastCode findByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId)
			throws ResourceNotFoundException {
		return voteCastCodeRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
	}

	/**
	 * @see VoteCastCodeRepository#save(String, String, String, VoteCastCode)
	 */
	@Override
	public void save(final String tenantId, final String electionEventId, final String votingCardId, final VoteCastCode voteCastCode) throws DuplicateEntryException {
		voteCastCodeRepository.save(tenantId, electionEventId, votingCardId, voteCastCode);
	}

}
