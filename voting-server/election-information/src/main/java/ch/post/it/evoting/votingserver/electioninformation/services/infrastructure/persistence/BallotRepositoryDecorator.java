/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;

/**
 * Decorator for the ballot repository.
 */
@Decorator
public abstract class BallotRepositoryDecorator implements BallotRepository {

	@Inject
	@Delegate
	private BallotRepository ballotRepository;

	/**
	 * @see BallotRepository#findByTenantIdElectionEventIdBallotId(String, String, String)
	 */
	@Override
	public Ballot findByTenantIdElectionEventIdBallotId(final String tenantId, final String electionEventId, final String ballotId) throws ResourceNotFoundException {
		return ballotRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
	}

	@Override
	public Ballot save(final Ballot ballot) throws DuplicateEntryException {
		return ballotRepository.save(ballot);
	}
}
