/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMapping;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMappingRepository;

/**
 * Implementation of the repository with JPA
 */
@Stateless
public class CodesMappingRepositoryImpl extends BaseRepositoryImpl<CodesMapping, Integer> implements CodesMappingRepository {

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_TENANT_ID = "tenantId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_VERIFICATION_CART_ID = "verificationCardId";

	/**
	 * Searches for an codees mapping with the given tenant, election event and verification card ids.
	 * This implementation uses database access by executing a SQL-query to select the data to be
	 * retrieved.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the electionEvent.
	 * @param verificationCardId - the identifier of the verificationCard.
	 * @return a entity representing the codes mapping.
	 */
	@Override
	public CodesMapping findByTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException {
		TypedQuery<CodesMapping> query = entityManager.createQuery(
				"SELECT a FROM CodesMapping a WHERE a.tenantId = :tenantId AND a.electionEventId = :electionEventId AND a.verificationCardId = :verificationCardId",
				CodesMapping.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CART_ID, verificationCardId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT COUNT(a) FROM CodesMapping a WHERE a.tenantId = :tenantId AND a.electionEventId = :electionEventId AND a.verificationCardId = :verificationCardId",
				Long.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CART_ID, verificationCardId);
		return query.getSingleResult() > 0;
	}
}
