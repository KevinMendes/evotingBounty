/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface for password repository for specific objects like ballot box.
 */
public interface PasswordForObjectRepository {

	/**
	 * Find a password string identified by this tenant id, election event id, object id
	 *
	 * @param tenantId the tenant identifier.
	 * @param eeId     the election event identifier.
	 * @param objectId the object identifier.
	 * @return the password as string
	 * @throws ResourceNotFoundException if the password for this tenant id and election event id can not be found.
	 */
	String getByTenantEEIDObjectId(String tenantId, String eeId, String objectId) throws ResourceNotFoundException;
}
