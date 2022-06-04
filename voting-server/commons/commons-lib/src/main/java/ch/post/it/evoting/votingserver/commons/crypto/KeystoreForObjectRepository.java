/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface defining the methods that can be used to retrieve keystores data for specific objects (e.g., ballot box).
 */
public interface KeystoreForObjectRepository {

	/**
	 * Find a keystore data string given this tenant, election event and object identifier (e.g., ballotBoxId).
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param objectId        - the object identifier (for instance, ballotBoxId).
	 * @return the keystore data represented as a string.
	 * @throws ResourceNotFoundException if the keystore data can not be found.
	 */
	String getJsonByTenantEEIDObjectId(String tenantId, String electionEventId, String objectId) throws ResourceNotFoundException;

}
