/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ejb.EJB;
import javax.json.JsonObject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectRepository;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

/**
 *
 */
public class AuthenticationKeyStoreForObjectRepository implements KeystoreForObjectRepository {

	// The encryption keystore name in the contents json.
	private static final String ENCRYPTION_KEYSTORE = "authenticationTokenSignerKeystore";

	// The verification content repository.
	@EJB
	AuthenticationContentRepository repository;

	/**
	 * Find a keystore data string given this tenant identifier, the electionEventIdentifier
	 *
	 * @param tenantId        the tenant identifier.
	 * @param electionEventId the election event identifier.
	 * @param objectId        - It doee not apply.
	 * @return the keystore data represented as a string.
	 * @throws ResourceNotFoundException if the keystore data can not be found.
	 * @see KeystoreForObjectRepository#getJsonByTenantEEIDObjectId(String, String, String)
	 */
	@Override
	public String getJsonByTenantEEIDObjectId(String tenantId, String electionEventId, String objectId) throws ResourceNotFoundException {

		// recover from db
		AuthenticationContent authenticationContent = repository.findByTenantIdElectionEventId(tenantId, electionEventId);

		// convert to json object
		JsonObject object = JsonUtils.getJsonObject(authenticationContent.getJson());

		return new String(Base64.getDecoder().decode(object.getString(ENCRYPTION_KEYSTORE)), StandardCharsets.UTF_8);

	}
}
