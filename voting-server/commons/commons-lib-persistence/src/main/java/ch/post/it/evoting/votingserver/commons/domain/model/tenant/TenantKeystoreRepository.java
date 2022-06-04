/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.tenant;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling the tenant keystores in each context
 */
@Local
public interface TenantKeystoreRepository extends BaseRepository<TenantKeystoreEntity, Long> {

	/**
	 * Returns the tenant keystores for a given tenant identifier and key type.
	 *
	 * @param tenantId tenant identifier.
	 * @param keyType  key type to search.
	 * @return the tenant keystore that matches with the parameters.
	 */
	TenantKeystoreEntity getByTenantAndType(String tenantId, String keyType);

	/**
	 * Check if a keystore of the specified type exists for the specified tenant.
	 *
	 * @param tenantId the ID of the tenant.
	 * @param keyType  the type of the private key that is stored within the keystore.
	 * @return true if such a keystore is found, false otherwise.
	 */
	boolean checkIfKeystoreExists(String tenantId, String keyType);
}
