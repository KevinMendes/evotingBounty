/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;

/**
 * The implementation of the operations on the verification repository. The implementation uses JPA
 * as data layer to access database.
 */
@Stateless
public class VerificationRepositoryImpl extends BaseRepositoryImpl<Verification, Integer> implements VerificationRepository {

	/* The name of parameter tenant id */
	private static final String PARAMETER_TENANT_ID = "tenantId";

	/* The name of parameter election event id */
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	/* The name of parameter verification card id */
	private static final String PARAMETER_VERIFICATION_CARD_ID = "verificationCardId";

	/**
	 * Returns a verification data for a given tenant, election event and verification card. In this
	 * implementation, we use a database query to obtain verification data taking into account the
	 * input parameters.
	 *
	 * @param tenantId           - the tenant identifier.
	 * @param electionEventId    - the election event identifier.
	 * @param verificationCardId - the verification card identifier.
	 * @return The verification data.
	 * @throws ResourceNotFoundException if the verification data is not found.
	 */
	@Override
	public Verification findByTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException {
		TypedQuery<Verification> query = entityManager.createQuery(
				"SELECT v FROM Verification v WHERE v.tenantId = :tenantId AND v.electionEventId = :electionEventId AND v.verificationCardId = :verificationCardId",
				Verification.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_ID, verificationCardId);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVerificationCardId(String tenantId, String electionEventId, String verificationCardId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT COUNT(v) FROM Verification v WHERE v.tenantId = :tenantId AND v.electionEventId = :electionEventId AND v.verificationCardId = :verificationCardId",
				Long.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VERIFICATION_CARD_ID, verificationCardId);
		return query.getSingleResult() > 0;
	}
}
