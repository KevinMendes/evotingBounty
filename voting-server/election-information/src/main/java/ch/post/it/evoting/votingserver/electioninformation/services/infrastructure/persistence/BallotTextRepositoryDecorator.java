/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotText;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotTextRepository;

/**
 * Decorator for the ballottext repository.
 */
@Decorator
public abstract class BallotTextRepositoryDecorator implements BallotTextRepository {

	private static final String ADMIN = "admin";

	@Inject
	@Delegate
	private BallotTextRepository ballotTextRepository;

	/**
	 * @see BallotRepository#findByTenantIdElectionEventIdBallotId(String, String, String)
	 */
	@Override
	public BallotText findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId)
			throws ResourceNotFoundException {
		return ballotTextRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
	}

	@Override
	public BallotText save(final BallotText ballotText) throws DuplicateEntryException {
		return ballotTextRepository.save(ballotText);
	}
}
