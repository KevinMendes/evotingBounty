/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;

/**
 * Implementation of the repository with JPA
 */
@Decorator
public abstract class VerificationRepositoryDecorator implements VerificationRepository {

	@Inject
	@Delegate
	private VerificationRepository verificationRepository;

	@Override
	public Verification save(final Verification entity) throws DuplicateEntryException {
		return verificationRepository.save(entity);
	}

	@Override
	public Verification findByTenantIdElectionEventIdVerificationCardId(final String tenantId, final String electionEventId,
			final String verificationCardId) throws ResourceNotFoundException {
		return verificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVerificationCardId(final String tenantId, final String electionEventId,
			final String verificationCardId) {
		return verificationRepository.hasWithTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);
	}
}
