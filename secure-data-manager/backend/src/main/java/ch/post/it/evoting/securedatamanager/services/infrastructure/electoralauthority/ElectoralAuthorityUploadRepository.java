/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority;

import java.io.IOException;

import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationResult;

/**
 * Implements the repository for electoral authority uploadDataInContext.
 */
@Repository
public class ElectoralAuthorityUploadRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectoralAuthorityUploadRepository.class);

	private static final String ADMIN_BOARD_ID_PARAM = "adminBoardId";
	private static final String ENDPOINT_CHECK_IF_EMPTY = "/electioneventdata/tenant/{tenantId}/electionevent/{electionEventId}/status";
	private static final String ELECTION_EVENT_ID_PARAM = "electionEventId";
	private static final String TENANT_ID_PARAM = "tenantId";
	private static final String UPLOAD_ELECTION_EVENT_DATA_URL = "/electioneventdata/tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}";

	@Value("${AU_URL}")
	private String authenticationURL;

	@Value("${tenantID}")
	private String tenantId;

	/**
	 * Uploads the authentication context data.
	 *
	 * @param electionEventId the election event identifier.
	 * @param adminBoardId    the admin board id.
	 * @param json            the json object containing the authentication context data. @return True if the information is successfully uploaded.
	 *                        Otherwise, false.
	 */
	public boolean uploadAuthenticationContextData(final String electionEventId, final String adminBoardId, final JsonObject json) {
		return uploadContextData(electionEventId, adminBoardId, json, authenticationURL);
	}

	private boolean uploadContextData(final String electionEventId, final String adminBoardId, final JsonObject json, final String url) {
		final WebTarget target = ClientBuilder.newClient().target(url);
		final Response response = target.path(UPLOAD_ELECTION_EVENT_DATA_URL).resolveTemplate(TENANT_ID_PARAM, tenantId)
				.resolveTemplate(ELECTION_EVENT_ID_PARAM, electionEventId).resolveTemplate(ADMIN_BOARD_ID_PARAM, adminBoardId).request()
				.post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON_TYPE));

		return response.getStatus() == Response.Status.OK.getStatusCode();
	}

	/**
	 * Checks if there is information for the specific election event ID in the authentication (AU) context.
	 *
	 * @param electionEvent - identifier of the election event
	 * @return true if there is information, false otherwise.
	 */
	public boolean checkEmptyElectionEventDataInAU(final String electionEvent) {
		return checkResult(electionEvent, authenticationURL);
	}

	private boolean checkResult(final String electionEvent, final String url) {
		boolean result = false;
		final WebTarget target = ClientBuilder.newClient().target(url + ENDPOINT_CHECK_IF_EMPTY);

		final Response response = target.resolveTemplate(TENANT_ID_PARAM, tenantId).resolveTemplate(ELECTION_EVENT_ID_PARAM, electionEvent)
				.request(MediaType.APPLICATION_JSON).get();

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			final String json = response.readEntity(String.class);
			try {
				result = objectMapper.readValue(json, ValidationResult.class).isResult();
			} catch (final IOException e) {
				LOGGER.error("Error checking if a ballot box is empty", e);
			}
		}
		return result;
	}
}
