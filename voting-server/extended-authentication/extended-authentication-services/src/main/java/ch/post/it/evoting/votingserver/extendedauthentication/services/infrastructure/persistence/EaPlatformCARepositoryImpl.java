/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCAEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.extendedauthentication.services.domain.model.platform.EaPlatformCARepository;
import ch.post.it.evoting.votingserver.extendedauthentication.services.domain.model.platform.EaPlatformCaEntity;

/**
 * Implementation of the repository with JPA
 */
@Stateless
@EaPlatformCARepository
public class EaPlatformCARepositoryImpl extends BaseRepositoryImpl<PlatformCAEntity, Long> implements PlatformCARepository {

	/**
	 * @see PlatformCARepository#getRootCACertificate()
	 */
	@Override
	public PlatformCAEntity getRootCACertificate() throws ResourceNotFoundException {
		TypedQuery<EaPlatformCaEntity> query = entityManager.createQuery("SELECT a FROM EaPlatformCaEntity a", EaPlatformCaEntity.class);
		try {
			return query.getResultList().get(0);
		} catch (NoResultException e) {

			throw new ResourceNotFoundException("", e);
		}
	}
}
