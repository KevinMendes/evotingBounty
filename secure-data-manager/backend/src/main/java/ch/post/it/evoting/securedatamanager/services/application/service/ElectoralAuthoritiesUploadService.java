/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityUploadRepository;

/**
 * Service which uploads files to voter portal after creating the electoral authorities
 */
@Service
public class ElectoralAuthoritiesUploadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectoralAuthoritiesUploadService.class);

	private static final String CONSTANT_AUTHENTICATION_CONTEXT_DATA = "authenticationContextData";
	private static final String CONSTANT_AUTHENTICATION_VOTER_DATA = "authenticationVoterData";

	private final ElectoralAuthorityRepository electoralAuthorityRepository;
	private final ElectoralAuthorityUploadRepository electoralAuthorityUploadRepository;
	private final PathResolver pathResolver;
	private final ElectionEventRepository electionEventRepository;

	public ElectoralAuthoritiesUploadService(final ElectoralAuthorityRepository electoralAuthorityRepository,
			final ElectoralAuthorityUploadRepository electoralAuthorityUploadRepository, final PathResolver pathResolver,
			final ElectionEventRepository electionEventRepository) {
		this.electoralAuthorityRepository = electoralAuthorityRepository;
		this.electoralAuthorityUploadRepository = electoralAuthorityUploadRepository;
		this.pathResolver = pathResolver;
		this.electionEventRepository = electionEventRepository;
	}

	/**
	 * Uploads the available electoral authorities texts to the voter portal.
	 */
	public void uploadSynchronizableElectoralAuthorities(final String electionEvent) throws IOException {

		final Map<String, Object> params = new HashMap<>();
		params.put(JsonConstants.STATUS, Status.SIGNED.name());
		params.put(JsonConstants.SYNCHRONIZED, SynchronizeStatus.PENDING.getIsSynchronized().toString());

		if (StringUtils.isNotEmpty(electionEvent)) {
			params.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEvent);
		}

		final String documents = electoralAuthorityRepository.list(params);
		final JsonArray electoralAuthorities = JsonUtils.getJsonObject(documents).getJsonArray(JsonConstants.RESULT);

		for (int i = 0; i < electoralAuthorities.size(); i++) {

			final JsonObject electoralAuthoritiesInArray = electoralAuthorities.getJsonObject(i);
			final String electoralAuthorityId = electoralAuthoritiesInArray.getString(JsonConstants.ID);
			final String electionEventId = electoralAuthoritiesInArray.getJsonObject(JsonConstants.ELECTION_EVENT).getString(JsonConstants.ID);
			final JsonObject eEvent = JsonUtils.getJsonObject(electionEventRepository.find(electionEventId));
			final JsonObject adminBoard = eEvent.getJsonObject(JsonConstants.ADMINISTRATION_AUTHORITY);
			final String adminBoardId = adminBoard.getString(JsonConstants.ID);

			LOGGER.info("Uploading the signed authentication context configuration. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
					electoralAuthorityId);
			final boolean authResult = uploadAuthenticationContextData(electionEventId, adminBoardId);

			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			if (authResult) {
				LOGGER.info("Changing the status of the electoral authority. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
						electoralAuthorityId);
				jsonObjectBuilder.add(JsonConstants.ID, electoralAuthorityId);
				jsonObjectBuilder.add(JsonConstants.SYNCHRONIZED, SynchronizeStatus.SYNCHRONIZED.getIsSynchronized().toString());
				jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.SYNCHRONIZED.getStatus());
				LOGGER.info("The electoral authority was uploaded successfully. [electionEventId: {}, electoralAuthorityId: {}]", electionEventId,
						electoralAuthorityId);
			} else {
				LOGGER.error("An error occurred while uploading the signed electoral authority. [electionEventId: {}, electoralAuthorityId: {}]",
						electionEventId, electoralAuthorityId);
				jsonObjectBuilder.add(JsonConstants.ID, electoralAuthorityId);
				jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.FAILED.getStatus());

			}

			try {
				electoralAuthorityRepository.update(jsonObjectBuilder.build().toString());
			} catch (final DatabaseException ex) {
				LOGGER.error(String.format(
						"An error occurred while updating the signed electoral authority. [electionEventId: %s, electoralAuthorityId: %s]",
						electionEventId, electoralAuthorityId), ex);
			}
		}

	}

	private boolean uploadAuthenticationContextData(final String electionEventId, final String adminBoardId) throws IOException {

		if (electoralAuthorityUploadRepository.checkEmptyElectionEventDataInAU(electionEventId)) {
			final Path authenticationPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
					Constants.CONFIG_DIR_NAME_AUTHENTICATION);

			final Path authenticationContextPath = pathResolver.resolve(authenticationPath.toString(),
					Constants.CONFIG_FILE_NAME_SIGNED_AUTH_CONTEXT_DATA);
			final Path authenticationVoterDataPath = pathResolver.resolve(authenticationPath.toString(),
					Constants.CONFIG_FILE_NAME_SIGNED_AUTH_VOTER_DATA);
			final JsonObject authenticationContexData = JsonUtils
					.getJsonObject(new String(Files.readAllBytes(authenticationContextPath), StandardCharsets.UTF_8));
			final JsonObject authenticationVoterData = JsonUtils
					.getJsonObject(new String(Files.readAllBytes(authenticationVoterDataPath), StandardCharsets.UTF_8));
			final JsonObject jsonInput = Json.createObjectBuilder().add(CONSTANT_AUTHENTICATION_CONTEXT_DATA, authenticationContexData.toString())
					.add(CONSTANT_AUTHENTICATION_VOTER_DATA, authenticationVoterData.toString()).build();

			return electoralAuthorityUploadRepository.uploadAuthenticationContextData(electionEventId, adminBoardId, jsonInput);
		}

		return true;
	}

}
