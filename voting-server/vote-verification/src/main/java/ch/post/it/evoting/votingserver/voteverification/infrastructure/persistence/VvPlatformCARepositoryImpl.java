/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.platform.PlatformCertificate;
import ch.post.it.evoting.votingserver.voteverification.domain.model.platform.VvPlatformCARepository;

/**
 * Implementation of the repository with JPA
 */
@Stateless
@VvPlatformCARepository
public class VvPlatformCARepositoryImpl extends BaseRepositoryImpl<PlatformCAEntity, Long> implements PlatformCARepository {

	/**
	 * @see PlatformCARepository#getRootCACertificate()
	 */
	@Override
	public PlatformCAEntity getRootCACertificate() throws ResourceNotFoundException {
		TypedQuery<PlatformCertificate> query = entityManager.createQuery("SELECT a FROM PlatformCertificate a", PlatformCertificate.class);
		try {
			return query.getResultList().get(0);
		} catch (NoResultException e) {

			throw new ResourceNotFoundException("", e);
		}
	}
}
