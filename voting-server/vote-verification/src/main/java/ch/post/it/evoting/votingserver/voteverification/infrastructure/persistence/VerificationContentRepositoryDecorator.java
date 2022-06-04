/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;

/**
 * Implementation of the repository with JPA
 */
@Decorator
public abstract class VerificationContentRepositoryDecorator implements VerificationContentRepository {

	@Inject
	@Delegate
	private VerificationContentRepository verificationContentRepository;

	@Override
	public VerificationContent find(final Integer verificationContentEntityId) {
		return verificationContentRepository.find(verificationContentEntityId);
	}

	@Override
	public VerificationContent save(final VerificationContent entity) throws DuplicateEntryException {
		return verificationContentRepository.save(entity);
	}

	@Override
	public VerificationContent findByTenantIdElectionEventIdVerificationCardSetId(final String tenantId, final String electionEventId,
			final String verificationCardSetId) throws ResourceNotFoundException {
		return verificationContentRepository.findByTenantIdElectionEventIdVerificationCardSetId(tenantId, electionEventId, verificationCardSetId);
	}
}
