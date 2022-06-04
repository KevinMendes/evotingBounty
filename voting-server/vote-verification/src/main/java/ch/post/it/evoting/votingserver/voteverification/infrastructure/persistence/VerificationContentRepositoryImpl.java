/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;

/**
 * Implementation of the repository with JPA
 */
@Stateless
public class VerificationContentRepositoryImpl extends BaseRepositoryImpl<VerificationContent, Integer> implements VerificationContentRepository {

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_TENANT_ID = "tenantId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_VERIFICATION_CART_SET_ID = "verificationCardSetId";

	/**
	 * Searches for an verification content with the given tenant, election event and verification
	 * card set ids. This implementation uses database access by executing a SQL-query to select the
	 * data to be retrieved.
	 *
	 * @param tenantId              - the identifier of the tenant.
	 * @param electionEventId       - the identifier of the electionEvent.
	 * @param verificationCardSetId - the identifier of the verificationCardSet.
	 * @return a entity representing the verification content.
	 */
	@Override
	public VerificationContent findByTenantIdElectionEventIdVerificationCardSetId(String tenantId, String electionEventId,
			String verificationCardSetId) throws ResourceNotFoundException {
		TypedQuery<VerificationContent> query = entityManager.createQuery(
				"SELECT a FROM VerificationContent a WHERE a.tenantId = :tenantId AND a.electionEventId = :electionEventId AND a.verificationCardSetId = :verificationCardSetId",
				VerificationContent.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CART_SET_ID, verificationCardSetId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}
}
