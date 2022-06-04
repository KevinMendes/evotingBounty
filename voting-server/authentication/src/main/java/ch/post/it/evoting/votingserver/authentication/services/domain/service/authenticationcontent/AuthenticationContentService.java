/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

/**
 * Service which retrieves the Authentication Content.
 */
@Stateless
public class AuthenticationContentService {

	private static final String KEYSTORE = "authenticationTokenSignerKeystore";
	private static final String CHALLENGE_LENGTH = "challengeLength";
	private static final String TOKEN_EXPIRATION_TIME = "authTokenExpTime";
	private static final String CHALLENGE_EXPIRATION_TIME = "challengeResExpTime";
	private static final String KEYSTORE_PW = "authenticationTokenSignerPassword";
	private static final String PARAMS = "authenticationParams";

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationContentService.class);

	@Inject
	private AuthenticationContentRepository authenticationContentRepository;

	/**
	 * Returns the authentication content for a given tenant and election event.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @return An AuthenticationContent with the keystore and authentication parameters.
	 * @throws ResourceNotFoundException if the authentication content is not found.
	 */
	public ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent getAuthenticationContent(String tenantId,
			String electionEventId) throws ResourceNotFoundException {

		LOGGER.info("Recovering AuthenticationContent. [tenantId: {}, electionEventId: {}]", tenantId, electionEventId);

		AuthenticationContent authenticationContent = authenticationContentRepository.findByTenantIdElectionEventId(tenantId, electionEventId);

		// convert to JSON object
		JsonObject object = JsonUtils.getJsonObject(authenticationContent.getJson());
		JsonObject params = object.getJsonObject(PARAMS);

		// create authentication content
		ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent result = new ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent();
		result.setChallengeExpirationTime(Integer.parseInt(params.getString(CHALLENGE_EXPIRATION_TIME)));
		result.setTokenExpirationTime(Integer.parseInt(params.getString(TOKEN_EXPIRATION_TIME)));
		result.setChallengeLength(Integer.parseInt(params.getString(CHALLENGE_LENGTH)));
		result.setPassword(object.getString(KEYSTORE_PW));
		String keyStore = new String(Base64.getDecoder().decode(object.getString(KEYSTORE)), StandardCharsets.UTF_8);
		result.setKeystore(JsonUtils.getJsonObject(keyStore));

		LOGGER.info("AuthenticationContent found. [tenantId: {}, electionEvent: {}]", tenantId, electionEventId);

		return result;
	}
}
