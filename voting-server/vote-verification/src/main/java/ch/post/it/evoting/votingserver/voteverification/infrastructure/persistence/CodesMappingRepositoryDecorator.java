/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMapping;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMappingRepository;

/**
 * Implementation of the repository with JPA
 */
@Decorator
public abstract class CodesMappingRepositoryDecorator implements CodesMappingRepository {

	@Inject
	@Delegate
	private CodesMappingRepository codesMappingRepository;

	@Override
	public CodesMapping save(final CodesMapping entity) throws DuplicateEntryException {
		return codesMappingRepository.save(entity);
	}

	@Override
	public CodesMapping findByTenantIdElectionEventIdVerificationCardId(final String tenantId, final String electionEventId,
			final String verificationCardId) throws ResourceNotFoundException {
		return codesMappingRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVerificationCardId(final String tenantId, final String electionEventId,
			final String verificationCardId) {
		return codesMappingRepository.hasWithTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);
	}
}
