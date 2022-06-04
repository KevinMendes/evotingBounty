/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.BallotBox;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * Service to operate with ballot boxes
 */
@Service
public class BallotBoxService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxService.class);

	// This insignificant lines correspond to the signature included in a downloaded ballot box file.
	private static final long INSIGNIFICANT_NUMBER_OF_LINES_IN_DOWNLOADED_BALLOT_BOX_FILE = 1L;

	@Autowired
	private ConfigurationEntityStatusService statusService;

	@Autowired
	private BallotBoxRepository ballotBoxRepository;

	@Autowired
	private PathResolver pathResolver;

	private ConfigObjectMapper mapper;

	@PostConstruct
	public void init() {
		mapper = new ConfigObjectMapper();
	}

	/**
	 * Sign the ballot box configuration and change the state of the ballot box from ready to SIGNED for a given election event and ballot box id.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotBoxId     the ballot box id.
	 * @param privateKeyPEM   the administration board private key in PEM format.
	 * @throws ResourceNotFoundException if the ballot box is not found.
	 * @throws GeneralCryptoLibException if the private key cannot be read.
	 */
	public void sign(final String electionEventId, final String ballotBoxId, final String privateKeyPEM)
			throws ResourceNotFoundException, GeneralCryptoLibException, IOException {

		final PrivateKey privateKey = PemUtils.privateKeyFromPem(privateKeyPEM);

		final JsonObject ballotBoxJsonObject = getValidBallotBox(electionEventId, ballotBoxId);
		final String ballotId = ballotBoxJsonObject.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);

		final Path ballotBoxConfigurationFilesPath = pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);

		LOGGER.info("Signing ballot box {}", ballotBoxId);
		signBallotBoxJSON(privateKey, ballotBoxConfigurationFilesPath);

		LOGGER.info("Changing the ballot box status");
		statusService.updateWithSynchronizedStatus(Status.SIGNED.name(), ballotBoxId, ballotBoxRepository, SynchronizeStatus.PENDING);

		LOGGER.info("The ballot box was successfully signed");

	}

	private void signBallotBoxJSON(final PrivateKey privateKey, final Path ballotBoxConfigurationFilesPath) throws IOException {
		final Path ballotBoxJSONPath = ballotBoxConfigurationFilesPath.resolve(Constants.CONFIG_DIR_NAME_BALLOTBOX_JSON).toAbsolutePath();
		final Path signedBallotBoxJSONPath = ballotBoxConfigurationFilesPath.resolve(Constants.CONFIG_FILE_NAME_SIGNED_BALLOTBOX_JSON)
				.toAbsolutePath();

		final BallotBox ballotBox = mapper.fromJSONFileToJava(new File(ballotBoxJSONPath.toString()), BallotBox.class);

		final String signedBallotBox = JsonSignatureService.sign(privateKey, ballotBox);
		final SignedObject signedBallotBoxObject = new SignedObject();
		signedBallotBoxObject.setSignature(signedBallotBox);
		mapper.fromJavaToJSONFile(signedBallotBoxObject, new File(signedBallotBoxJSONPath.toString()));
	}

	private JsonObject getValidBallotBox(final String electionEventId, final String ballotBoxId) throws ResourceNotFoundException {

		final Optional<JsonObject> possibleBallotBox = getPossibleValidBallotBox(electionEventId, ballotBoxId);

		if (!possibleBallotBox.isPresent()) {
			throw new ResourceNotFoundException("Ballot box not found");
		}

		return possibleBallotBox.get();
	}

	private Optional<JsonObject> getPossibleValidBallotBox(final String electionEventId, final String ballotBoxId) {

		Optional<JsonObject> ballotBox = Optional.empty();

		final Map<String, Object> attributeValueMap = new HashMap<>();
		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		attributeValueMap.put(JsonConstants.ID, ballotBoxId);
		attributeValueMap.put(JsonConstants.STATUS, Status.READY.name());
		final String ballotBoxResultListAsJson = ballotBoxRepository.list(attributeValueMap);
		if (StringUtils.isEmpty(ballotBoxResultListAsJson)) {
			return ballotBox;
		} else {
			final JsonArray ballotBoxResultList = JsonUtils.getJsonObject(ballotBoxResultListAsJson).getJsonArray(JsonConstants.RESULT);
			// Assume that there is just one element as result of the search.
			if (ballotBoxResultList != null && !ballotBoxResultList.isEmpty()) {
				ballotBox = Optional.of(ballotBoxResultList.getJsonObject(0));
			} else {
				return ballotBox;
			}
		}
		return ballotBox;
	}

	/**
	 * query ballot boxes to process in the synchronization process
	 *
	 * @return JsonArray with ballot boxes to upload
	 */
	public JsonArray getBallotBoxesReadyToSynchronize(final String electionEvent) {

		final Map<String, Object> params = new HashMap<>();

		params.put(JsonConstants.STATUS, Status.SIGNED.name());
		params.put(JsonConstants.SYNCHRONIZED, SynchronizeStatus.PENDING.getIsSynchronized().toString());
		// If there is an election event as parameter, it will be included in
		// the query
		if (!Constants.NULL_ELECTION_EVENT_ID.equals(electionEvent)) {
			params.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEvent);
		}
		final String serializedBallotBoxes = ballotBoxRepository.list(params);

		return JsonUtils.getJsonObject(serializedBallotBoxes).getJsonArray(JsonConstants.RESULT);
	}

	/**
	 * Updates the state of the synchronization status of the ballot box
	 */
	public void updateSynchronizationStatus(final String ballotBoxId, final boolean success) {
		final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add(JsonConstants.ID, ballotBoxId);
		if (success) {
			jsonObjectBuilder.add(JsonConstants.SYNCHRONIZED, SynchronizeStatus.SYNCHRONIZED.getIsSynchronized().toString());
			jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.SYNCHRONIZED.getStatus());
		} else {
			jsonObjectBuilder.add(JsonConstants.DETAILS, SynchronizeStatus.FAILED.getStatus());
		}
		ballotBoxRepository.update(jsonObjectBuilder.build().toString());
	}

	/**
	 * Checks that the ballot box has status Status.BB_DOWNLOADED
	 *
	 * @throws ResourceNotFoundException if the ballot box cannot be found in the ballot box repository.
	 */
	public boolean isDownloaded(final String ballotBoxId) throws ResourceNotFoundException {
		return hasStatus(ballotBoxId, Status.BB_DOWNLOADED);
	}

	/**
	 * Checks that the ballot box has status Status.BB_DOWNLOADED
	 *
	 * @throws ResourceNotFoundException if the ballot box cannot be found in the ballot box repository.
	 */
	public boolean hasStatus(final String ballotBoxId, final Status expectedStatus) throws ResourceNotFoundException {
		checkNotNull(ballotBoxId);
		validateUUID(ballotBoxId);

		final JsonObject ballotBox = getBallotBoxInfo(ballotBoxId);
		final String currentStatus;
		try {
			currentStatus = ballotBox.getString(JsonConstants.STATUS);
		} catch (final IllegalArgumentException e) {
			//Ballot box does not have a status field
			return false;
		}
		return Status.valueOf(currentStatus).equals(expectedStatus);
	}

	/**
	 * Gets the ballot Id associated associated with this ballot box
	 */
	public String getBallotId(final String ballotBoxId) throws ResourceNotFoundException {
		checkNotNull(ballotBoxId);
		validateUUID(ballotBoxId);

		final JsonObject ballotBox = getBallotBoxInfo(ballotBoxId);
		return ballotBox.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);
	}

	/**
	 * Returns the ballot box information as a JSON object.
	 */
	private JsonObject getBallotBoxInfo(final String ballotBoxId) throws ResourceNotFoundException {
		final JsonObject ballotBox = JsonUtils.getJsonObject(ballotBoxRepository.find(ballotBoxId));

		if (ballotBox == null || ballotBox.size() == 0) {
			throw new ResourceNotFoundException("Ballot box entity does not exist for id: " + ballotBoxId);
		}
		return ballotBox;
	}

	/**
	 * Checks if the ballot box is empty or not.
	 */
	public boolean isDownloadedBallotBoxEmpty(final String electionEventId, final String ballotId, final String ballotBoxId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		final Path ballotBoxPath = pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);
		final Path downloadedBallotBoxPath = ballotBoxPath.resolve(Constants.CONFIG_FILE_NAME_ELECTION_INFORMATION_DOWNLOADED_BALLOT_BOX);

		final long lineCount;
		try (final Stream<String> lines = Files.lines(downloadedBallotBoxPath)) {
			// We only count not empty or significant lines
			lineCount = lines.filter(line -> !line.isEmpty()).count();
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to read the downloaded ballot box CSV file", e);
		}

		// Must have smaller or equal number of lines than the insignificant number of lines in the downloaded ballot box file.
		return lineCount <= INSIGNIFICANT_NUMBER_OF_LINES_IN_DOWNLOADED_BALLOT_BOX_FILE;
	}

	/**
	 * Checks if the ballot box has confirmed votes.
	 */
	public boolean hasDownloadedBallotBoxConfirmedVotes(final String electionEventId, final String ballotId, final String ballotBoxId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		final Path ballotBoxPath = pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);
		final Path downloadedBallotBoxPath = ballotBoxPath.resolve(Constants.CONFIG_FILE_NAME_ELECTION_INFORMATION_DOWNLOADED_BALLOT_BOX);

		final long confirmedVoteCount;
		try (final Stream<String> lines = Files.lines(downloadedBallotBoxPath)) {
			confirmedVoteCount = lines.filter(line -> line.contains("encryptedOptions") && !line.contains("||")).count();
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to read the downloaded ballot box CSV file", e);
		}

		// Must have at least 1 confirmed vote.
		return confirmedVoteCount > 0;
	}
}
