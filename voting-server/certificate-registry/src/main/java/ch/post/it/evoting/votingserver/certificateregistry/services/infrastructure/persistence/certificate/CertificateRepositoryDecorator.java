/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.infrastructure.persistence.certificate;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;

/**
 * Decorator for CertificateRepository
 */
@Decorator
public abstract class CertificateRepositoryDecorator implements CertificateRepository {

	@Inject
	@Delegate
	private CertificateRepository certificateRepository;

	@Override
	public CertificateEntity save(final CertificateEntity entity) throws DuplicateEntryException {
		return certificateRepository.save(entity);
	}
}
