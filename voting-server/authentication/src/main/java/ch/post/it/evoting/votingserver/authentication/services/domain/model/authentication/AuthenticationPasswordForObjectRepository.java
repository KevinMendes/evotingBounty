/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import java.security.PrivateKey;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.json.JsonObject;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence.AuTenantSystemKeys;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.utils.PasswordEncrypter;
import ch.post.it.evoting.votingserver.commons.cache.Cache;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectRepository;
import ch.post.it.evoting.votingserver.commons.crypto.PasswordForObjectRepository;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

/**
 * Implementation of the repository with jpa
 */
public class AuthenticationPasswordForObjectRepository implements PasswordForObjectRepository {

	// The encryption keystore password name in the contents json.
	private static final String AUTHENTICATION_TOKEN_SIGNER_PW = "authenticationTokenSignerPassword";

	private static final String KEY_SEPERATOR = "-";

	@EJB
	AuthenticationContentRepository repository;

	@EJB
	private AuTenantSystemKeys auTenantSystemKeys;

	@Inject
	private AsymmetricServiceAPI asymmetricService;

	@EJB(beanName = "AuthTokenSignerKeystorePasswordCache")
	private Cache<String, String> passwordCache;

	/**
	 * Retrieves the password for one keystore given a tenant identifier, the electionEventIdentifier and the verification card set id.
	 *
	 * @param tenantId        the tenant identifier.
	 * @param electionEventId the election event identifier.
	 * @param id              - It does not apply
	 * @return the keystore data represented as a string.
	 * @throws ResourceNotFoundException if the keystore data can not be found.
	 * @see KeystoreForObjectRepository#getJsonByTenantEEIDObjectId(String, String, String)
	 */
	@Override
	public String getByTenantEEIDObjectId(final String tenantId, final String electionEventId, final String id) throws ResourceNotFoundException {

		final String cacheKey = constructCacheKey(tenantId, electionEventId);

		final String cachedPassword = passwordCache.get(cacheKey);
		if ((cachedPassword != null) && (!cachedPassword.isEmpty())) {
			return cachedPassword;
		}

		// recover from db
		final AuthenticationContent authenticationContent = repository.findByTenantIdElectionEventId(tenantId, electionEventId);

		// convert to json object
		final JsonObject object = JsonUtils.getJsonObject(authenticationContent.getJson());

		final String passwordFromDB = object.getString(AUTHENTICATION_TOKEN_SIGNER_PW);

		final String decryptedString;
		try {
			decryptedString = decryptedPassword(tenantId, passwordFromDB);
		} catch (final GeneralCryptoLibException e) {
			throw new ResourceNotFoundException(
					"Error while trying to recover tenant system password to decrypt the auth token signer keystore password", e);
		}

		passwordCache.put(cacheKey, decryptedString);

		return decryptedString;
	}

	private String decryptedPassword(final String tenantId, final String passwordFromDB) throws GeneralCryptoLibException {

		final PasswordEncrypter passwordEncrypter = new PasswordEncrypter(asymmetricService);

		final PrivateKey privateKey = auTenantSystemKeys.getEncryptionPrivateKey(tenantId);

		return passwordEncrypter.decryptPassword(passwordFromDB, privateKey);
	}

	private String constructCacheKey(final String tenantId, final String electionEventId) {

		return tenantId + KEY_SEPERATOR + electionEventId;
	}
}
