/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextRepository;

/**
 * Implementation of the repository with JPA
 */
public class ElectionEventContextRepositoryImpl extends BaseRepositoryImpl<ElectionEventContextEntity, Integer> implements ElectionEventContextRepository {

	@Override
	public ElectionEventContextEntity findByElectionEventId(String electionEventId) throws ResourceNotFoundException {
		TypedQuery<ElectionEventContextEntity> query = entityManager.createQuery(
				"SELECT a FROM ElectionEventContextEntity a WHERE a.electionEventId = :electionEventId", ElectionEventContextEntity.class);
		query.setParameter("electionEventId", electionEventId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException(String.format("No election event context found for electionElectionId: %s", electionEventId), e);
		}
	}
}
