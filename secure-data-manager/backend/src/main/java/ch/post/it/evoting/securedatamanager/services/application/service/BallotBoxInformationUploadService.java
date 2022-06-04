/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

/**
 * Service for uploading the ballot box information.
 */
@Service
public class BallotBoxInformationUploadService {

	public static final String ADMIN_BOARD_ID_PARAM = "adminBoardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxInformationUploadService.class);
	private static final String ENDPOINT_BALLOT_BOX_CONTENTS = "/ballotboxdata/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/adminboard/{adminBoardId}";
	private static final String ENDPOINT_CHECK_IF_BALLOT_BOXES_EMPTY = "/ballotboxes/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/status";
	private static final String CONSTANT_BALLOT_BOX_DATA = "ballotBox";
	private static final String TENANT_ID_PARAM = "tenantId";
	private static final String ELECTION_EVENT_ID_PARAM = "electionEventId";
	private static final String BALLOT_BOX_ID_PARAM = "ballotBoxId";

	private final ElectionEventRepository electionEventRepository;
	private final BallotBoxService ballotBoxService;
	private final PathResolver pathResolver;

	@Value("${EI_URL}")
	private String electionInformationBaseURL;

	@Value("${tenantID}")
	private String tenantId;

	public BallotBoxInformationUploadService(final ElectionEventRepository electionEventRepository,
			final BallotBoxService ballotBoxService,
			final PathResolver pathResolver) {
		this.electionEventRepository = electionEventRepository;
		this.ballotBoxService = ballotBoxService;
		this.pathResolver = pathResolver;
	}

	/**
	 * Uploads additional information of all ballot boxes pending for upload to the voting portal.
	 */
	public void uploadSynchronizableBallotBoxInformation(final String eeid) {

		final JsonArray ballotBoxes = ballotBoxService.getBallotBoxesReadyToSynchronize(eeid);

		for (int i = 0; i < ballotBoxes.size(); i++) {

			final JsonObject ballotBox = ballotBoxes.getJsonObject(i);

			final String ballotBoxId = ballotBox.getString(JsonConstants.ID);
			final String electionEventId = ballotBox.getJsonObject(JsonConstants.ELECTION_EVENT).getString(JsonConstants.ID);

			final JsonObject electionEvent = JsonUtils.getJsonObject(electionEventRepository.find(electionEventId));
			final JsonObject adminBoard = electionEvent.getJsonObject(JsonConstants.ADMINISTRATION_AUTHORITY);

			final String adminBoardId = adminBoard.getString(JsonConstants.ID);

			if (isBallotBoxEmpty(electionEventId, ballotBoxId)) {
				final String ballotId = ballotBox.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);

				try {
					final boolean uploadResultBBInformation = uploadBallotBoxConfiguration(electionEventId, ballotBoxId, ballotId, adminBoardId);

					uploadResultBallotBoxInformation(uploadResultBBInformation, ballotBoxId);

				} catch (final IOException e) {
					LOGGER.error(MessageFormat.format("Error trying to find ballot box configuration to upload for ballot box id {0}, skipping: {1} ",
							ballotBoxId, e));
				}
			} else {
				LOGGER.info("Updating the synchronization status of the ballot box");
				ballotBoxService.updateSynchronizationStatus(ballotBoxId, false);
			}
		}
	}

	/**
	 * Uploads ballot box configuration
	 */
	private boolean uploadBallotBoxConfiguration(final String electionEventId, final String ballotBoxId, final String ballotId,
			final String adminBoardId) throws IOException {

		LOGGER.info("Loading the signed ballot box configuration");
		final JsonObject ballotBoxConfiguration = loadFilesToUpload(electionEventId, ballotBoxId, ballotId);

		LOGGER.info("Uploading the signed ballot box configuration");
		final WebTarget target = ClientBuilder.newClient().target(electionInformationBaseURL + ENDPOINT_BALLOT_BOX_CONTENTS);
		final Response response = target.resolveTemplate(TENANT_ID_PARAM, tenantId).resolveTemplate(ELECTION_EVENT_ID_PARAM, electionEventId)
				.resolveTemplate(BALLOT_BOX_ID_PARAM, ballotBoxId).resolveTemplate(ADMIN_BOARD_ID_PARAM, adminBoardId).request()
				.post(Entity.entity(ballotBoxConfiguration.toString(), MediaType.APPLICATION_JSON_TYPE));

		return response.getStatus() == Response.Status.OK.getStatusCode();
	}

	private void uploadResultBallotBoxInformation(final boolean uploadResultBBInformation, final String ballotBoxId) {
		LOGGER.info("Updating the synchronization status of the ballot box");
		ballotBoxService.updateSynchronizationStatus(ballotBoxId, uploadResultBBInformation);
		LOGGER.info("The ballot box configuration was uploaded successfully");
	}

	/**
	 * Retrieve json files on filesystem to upload
	 */
	private JsonObject loadFilesToUpload(final String electionEventId, final String ballotBoxId, final String ballotId) throws IOException {

		final JsonFactory jsonFactory = new JsonFactory();
		final ObjectMapper jsonMapper = new ObjectMapper(jsonFactory);

		final Path ballotBoxConfigurationFilesPath = getCommonPathForConfigurationFiles(electionEventId, ballotBoxId, ballotId);
		final JsonNode ballotBoxNode = getBallotBoxSignature(jsonFactory, jsonMapper, ballotBoxConfigurationFilesPath);

		return (Json.createObjectBuilder().add(CONSTANT_BALLOT_BOX_DATA, ballotBoxNode.toString())).build();
	}

	private JsonNode getBallotBoxSignature(final JsonFactory jsonFactory, final ObjectMapper jsonMapper, final Path ballotBoxConfigurationFilesPath)
			throws IOException {
		return jsonMapper.readTree(jsonFactory.createParser(
				new File(ballotBoxConfigurationFilesPath.resolve(Constants.CONFIG_FILE_NAME_SIGNED_BALLOTBOX_JSON).toAbsolutePath().toString())));
	}

	private Path getCommonPathForConfigurationFiles(final String electionEventId, final String ballotBoxId, final String ballotId) {
		return pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION).resolve(Constants.CONFIG_DIR_NAME_BALLOTS).resolve(ballotId)
				.resolve(Constants.CONFIG_DIR_NAME_BALLOTBOXES).resolve(ballotBoxId);
	}

	private boolean isBallotBoxEmpty(final String electionEvent, final String ballotBoxId) {

		boolean result = Boolean.FALSE;
		final WebTarget target = ClientBuilder.newClient().target(electionInformationBaseURL + ENDPOINT_CHECK_IF_BALLOT_BOXES_EMPTY);

		final Response response = target.resolveTemplate(TENANT_ID_PARAM, tenantId).resolveTemplate(ELECTION_EVENT_ID_PARAM, electionEvent)
				.resolveTemplate(BALLOT_BOX_ID_PARAM, ballotBoxId).request(MediaType.APPLICATION_JSON).get();

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			final String json = response.readEntity(String.class);
			ValidationResult validationResult = new ValidationResult();
			try {
				validationResult = objectMapper.readValue(json, ValidationResult.class);
			} catch (final IOException e) {
				LOGGER.error("Error checking if a ballot box is empty", e);
			}
			result = validationResult.isResult();
		}
		return result;
	}
}
