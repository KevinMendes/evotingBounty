/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextRepository;

/**
 * Implementation of the repository with JPA
 */
@Decorator
public class ElectionEventContextRepositoryDecorator implements ElectionEventContextRepository {

	@Inject
	@Delegate
	private ElectionEventContextRepository electionEventContextRepository;

	@Override
	public ElectionEventContextEntity find(Integer integer) {
		return electionEventContextRepository.find(integer);
	}

	@Override
	public ElectionEventContextEntity save(ElectionEventContextEntity entity) throws DuplicateEntryException {
		return electionEventContextRepository.save(entity);
	}

	@Override
	public ElectionEventContextEntity update(ElectionEventContextEntity entity) throws EntryPersistenceException {
		return electionEventContextRepository.update(entity);
	}

	@Override
	public ElectionEventContextEntity findByElectionEventId(String electionEventId) throws ResourceNotFoundException {
		return electionEventContextRepository.findByElectionEventId(electionEventId);
	}
}
