/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface defining the methods that can be used to retrieve keystores data
 */
public interface KeystoreRepository {

	/**
	 * Find a keystore data string given this tenant identifier and the electionEventIdentifier.
	 *
	 * @param tenantId        the tenant identifier.
	 * @param electionEventId the election event identifier.
	 * @return the keystore data represented as a string.
	 * @throws ResourceNotFoundException if the keystore data can not be found.
	 */
	String getJsonByTenantEEID(String tenantId, String electionEventId) throws ResourceNotFoundException;

}
