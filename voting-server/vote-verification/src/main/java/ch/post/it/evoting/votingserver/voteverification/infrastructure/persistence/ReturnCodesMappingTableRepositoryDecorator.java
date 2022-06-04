/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableRepository;

/**
 * Implementation of the repository with JPA
 */
@Decorator
public class ReturnCodesMappingTableRepositoryDecorator implements ReturnCodesMappingTableRepository {

	@Inject
	@Delegate
	private ReturnCodesMappingTableRepository electionEventContextRepository;

	@Override
	public ReturnCodesMappingTableEntity find(final String verificationCardSetId) {
		return electionEventContextRepository.find(verificationCardSetId);
	}

	@Override
	public ReturnCodesMappingTableEntity save(final ReturnCodesMappingTableEntity entity) throws DuplicateEntryException {
		return electionEventContextRepository.save(entity);
	}

	@Override
	public ReturnCodesMappingTableEntity update(final ReturnCodesMappingTableEntity entity) throws EntryPersistenceException {
		return electionEventContextRepository.update(entity);
	}
}