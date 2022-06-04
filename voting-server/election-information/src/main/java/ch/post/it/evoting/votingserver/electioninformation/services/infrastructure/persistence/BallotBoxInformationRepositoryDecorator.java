/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import java.io.IOException;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;

/**
 * Decorator of the ballot box information repository.
 */
@Decorator
public abstract class BallotBoxInformationRepositoryDecorator implements BallotBoxInformationRepository {

	@Inject
	@Delegate
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	@Override
	public BallotBoxInformation findByTenantIdElectionEventIdBallotBoxId(final String tenantId, final String electionEventId, final String ballotBoxId)
			throws ResourceNotFoundException {
		return ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId);
	}

	@Override
	public void addBallotBoxInformation(final String tenantId, final String electionEventId, final String ballotBoxId, final String jsonContent)
			throws DuplicateEntryException, IOException {
		ballotBoxInformationRepository.addBallotBoxInformation(tenantId, electionEventId, ballotBoxId, jsonContent);
	}

	@Override
	public BallotBoxInformation save(final BallotBoxInformation ballotBoxInformation) throws DuplicateEntryException {
		return ballotBoxInformationRepository.save(ballotBoxInformation);
	}

	@Override
	public BallotBoxInformation update(final BallotBoxInformation ballotBoxInformation) throws EntryPersistenceException {
		return ballotBoxInformationRepository.update(ballotBoxInformation);
	}
}
