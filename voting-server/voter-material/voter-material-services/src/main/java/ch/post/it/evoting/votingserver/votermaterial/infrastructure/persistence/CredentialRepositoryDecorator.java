/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.Credential;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialRepository;

/**
 * A decorator with logger for voterIinformation repository.
 */
@Decorator
public abstract class CredentialRepositoryDecorator implements CredentialRepository {

	@Inject
	@Delegate
	private CredentialRepository credentialRepository;

	@Override
	public Credential save(final Credential entity) throws DuplicateEntryException {
		return credentialRepository.save(entity);
	}

	@Override
	public Credential findByTenantIdElectionEventIdCredentialId(final String tenantId, final String electionEventId, final String credentialId)
			throws ResourceNotFoundException {
		return credentialRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdCredentialId(final String tenantId, final String electionEventId, final String credentialId) {
		return credentialRepository.hasWithTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);
	}
}
