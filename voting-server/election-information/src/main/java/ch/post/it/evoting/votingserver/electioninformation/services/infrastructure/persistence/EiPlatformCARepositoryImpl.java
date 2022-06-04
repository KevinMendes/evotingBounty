/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.platform.EiPlatformCARepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.platform.PlatformCertificate;

/**
 * Implementation of the repository with JPA
 */
@Stateless
@EiPlatformCARepository
public class EiPlatformCARepositoryImpl extends BaseRepositoryImpl<PlatformCAEntity, Long> implements PlatformCARepository {

	/**
	 * @see PlatformCARepository#getRootCACertificate()
	 */
	@Override
	public PlatformCAEntity getRootCACertificate() throws ResourceNotFoundException {
		final TypedQuery<PlatformCertificate> query = entityManager.createQuery("SELECT a FROM PlatformCertificate a", PlatformCertificate.class);
		try {
			return query.getResultList().get(0);
		} catch (final NoResultException e) {

			throw new ResourceNotFoundException("", e);
		}
	}
}
