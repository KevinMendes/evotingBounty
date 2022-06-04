/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.Credential;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialRepository;

/**
 * The implementation of the operations on the credential repository. The implementation uses JPA as
 * data layer to access database.
 */
@Stateless
public class CredentialRepositoryImpl extends BaseRepositoryImpl<Credential, Integer> implements CredentialRepository {

	/* The name of parameter tenant id */
	private static final String PARAMETER_TENANT_ID = "tenantId";

	/* The name of parameter election event id */
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	/* The name of parameter voting card id */
	private static final String PARAMETER_CREDENTIAL_ID = "credentialId";

	/**
	 * Returns a credential data for a given tenant, election event and voting card. In this
	 * implementation, we use a database query to obtain credential data taking into account the input
	 * parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @return The credential data.
	 * @throws ResourceNotFoundException if credential data is not found.
	 */
	@Override
	public Credential findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException {
		TypedQuery<Credential> query = entityManager.createQuery(
				"SELECT c FROM Credential c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.credentialId = :credentialId",
				Credential.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_CREDENTIAL_ID, credentialId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT COUNT(c) FROM Credential c WHERE c.tenantId = :tenantId AND c.electionEventId = :electionEventId AND c.credentialId = :credentialId",
				Long.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_CREDENTIAL_ID, credentialId);
		return query.getSingleResult() > 0;
	}
}
