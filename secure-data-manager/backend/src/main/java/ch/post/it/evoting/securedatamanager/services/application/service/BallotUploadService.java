/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotUploadRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

/**
 * Service responsible of uploading ballots and ballot texts
 */
@Service
public class BallotUploadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotUploadService.class);

	private static final String NULL_ELECTION_EVENT_ID = "";

	private static final String BALLOT_ID_FIELD = "ballot.id";

	@Autowired
	private ElectionEventRepository electionEventRepository;

	@Autowired
	private BallotRepository ballotRepository;

	@Autowired
	private BallotTextRepository ballotTextRepository;

	@Autowired
	private BallotUploadRepository ballotUploadRepository;

	/**
	 * Uploads the available ballots and ballot texts to the voter portal.
	 */
	public void uploadSynchronizableBallots(final String eeid) {

		final Map<String, Object> ballotParams = new HashMap<>();

		addSignedBallots(ballotParams);
		addPendingToSynchBallots(ballotParams);

		if (thereIsAnElectionEventAsAParameter(eeid)) {
			addBallotsOfElectionEvent(eeid, ballotParams);
		}

		final String ballotDocuments = ballotRepository.list(ballotParams);
		final JsonArray ballots = JsonUtils.getJsonObject(ballotDocuments).getJsonArray(JsonConstants.RESULT);

		for (int i = 0; i < ballots.size(); i++) {

			final JsonObject ballotInArray = ballots.getJsonObject(i);
			final String signedBallot = ballotInArray.getString(JsonConstants.SIGNED_OBJECT);

			final String electionEventId = ballotInArray.getJsonObject(JsonConstants.ELECTION_EVENT).getString(JsonConstants.ID);
			final String ballotId = ballotInArray.getString(JsonConstants.ID);

			final JsonObject electionEvent = JsonUtils.getJsonObject(electionEventRepository.find(electionEventId));
			final JsonObject adminBoard = electionEvent.getJsonObject(JsonConstants.ADMINISTRATION_AUTHORITY);

			final String adminBoardId = adminBoard.getString(JsonConstants.ID);

			final Map<String, Object> ballotTextParams = new HashMap<>();
			ballotTextParams.put(BALLOT_ID_FIELD, ballotId);

			LOGGER.info("Loading the signed ballot");
			final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
			for (final String signature : ballotTextRepository.listSignatures(ballotTextParams)) {
				final JsonObject object = Json.createObjectBuilder().add(JsonConstants.SIGNED_OBJECT, signature).build();
				arrayBuilder.add(object);
			}
			final JsonArray signedBallotTexts = arrayBuilder.build();
			LOGGER.info("Loading the signed ballot texts");
			final JsonObject jsonInput = Json.createObjectBuilder().add(JsonConstants.BALLOT, signedBallot)
					.add(JsonConstants.BALLOTTEXT, signedBallotTexts.toString()).build();

			LOGGER.info("Uploading the signed ballot and ballot texts");
			final boolean result = ballotUploadRepository.uploadBallot(jsonInput, electionEventId, ballotId, adminBoardId);
			final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
			if (result) {

				LOGGER.info("Changing the state of the ballot");
				jsonObjectBuilder.add(JsonConstants.ID, ballotId);
				jsonObjectBuilder.add(JsonConstants.SYNCHRONIZED, SynchronizeStatus.SYNCHRONIZED.getIsSynchronized().toString());
				jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.SYNCHRONIZED.getStatus());
				LOGGER.info("The signed ballot was uploaded successfully");

			} else {
				final String error = "An error occurred while uploading the signed ballot";
				LOGGER.error(error);
				jsonObjectBuilder.add(JsonConstants.ID, ballotId);
				jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.FAILED.getStatus());

			}

			try {
				ballotRepository.update(jsonObjectBuilder.build().toString());
			} catch (final DatabaseException ex) {
				LOGGER.error("An error occurred while updating the signed ballot", ex);
			}

		}

	}

	private void addBallotsOfElectionEvent(final String electionEvent, final Map<String, Object> ballotParams) {
		ballotParams.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEvent);
	}

	private void addPendingToSynchBallots(final Map<String, Object> ballotParams) {
		ballotParams.put(JsonConstants.SYNCHRONIZED, SynchronizeStatus.PENDING.getIsSynchronized().toString());
	}

	private void addSignedBallots(final Map<String, Object> ballotParams) {
		ballotParams.put(JsonConstants.STATUS, Status.SIGNED.name());
	}

	private boolean thereIsAnElectionEventAsAParameter(final String electionEvent) {
		return !NULL_ELECTION_EVENT_ID.equals(electionEvent);
	}

}
