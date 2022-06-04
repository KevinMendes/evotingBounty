/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreEntity;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.orchestrator.domain.model.tenant.OrTenantKeystoreRepository;
import ch.post.it.evoting.votingserver.orchestrator.domain.model.tenant.TenantKeystore;

@Stateless
@OrTenantKeystoreRepository
public class OrTenantKeystoreRepositoryImpl extends BaseRepositoryImpl<TenantKeystoreEntity, Long> implements TenantKeystoreRepository {

	private static final String PARAMETER_KEY_TYPE = "keyType";

	private static final String PARAMETER_TENANT_ID = "tenantId";

	private static final int INDEX_OF_RESULT_ROW_TO_USE = 0;

	/**
	 * @see TenantKeystoreRepository#getByTenantAndType(String, String)
	 */
	@Override
	public TenantKeystoreEntity getByTenantAndType(final String tenantId, final String keyType) {

		final TypedQuery<TenantKeystore> query = entityManager
				.createQuery("SELECT a FROM TenantKeystore a where a.keyType=:keyType AND a.tenantId=:tenantId", TenantKeystore.class);

		query.setParameter(PARAMETER_KEY_TYPE, keyType);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);

		return query.getResultList().get(INDEX_OF_RESULT_ROW_TO_USE);
	}

	/**
	 * @see TenantKeystoreRepository#checkIfKeystoreExists(String, String)
	 */
	@Override
	public boolean checkIfKeystoreExists(final String tenantId, final String keyType) {

		final TypedQuery<TenantKeystore> query = entityManager
				.createQuery("SELECT a FROM TenantKeystore a where a.keyType=:keyType AND a.tenantId=:tenantId", TenantKeystore.class);
		query.setParameter(PARAMETER_KEY_TYPE, keyType);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);

		return query.getResultList().size() == 1;
	}
}
