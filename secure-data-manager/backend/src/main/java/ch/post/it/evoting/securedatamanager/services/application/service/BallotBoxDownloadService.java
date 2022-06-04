/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.ElectionInformationClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.OrchestratorClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.tally.MixnetInitialPayloadFileRepository;
import ch.post.it.evoting.securedatamanager.tally.MixnetShufflePayloadFileRepository;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Service for handling download encrypted ballot boxes.
 */
@Service
public class BallotBoxDownloadService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxDownloadService.class);

	private final BallotBoxRepository ballotBoxRepository;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final PathResolver pathResolver;
	private final KeyStoreService keystoreService;
	private final MixnetInitialPayloadFileRepository mixnetInitialPayloadFileRepository;
	private final MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository;
	private final ObjectMapper objectMapper;
	private final String orchestratorUrl;
	private final String electionInformationUrl;
	private final String tenantId;

	private Retrofit orchestratorRestAdapter;
	private Retrofit electionInformationRestAdapter;

	public BallotBoxDownloadService(final BallotBoxRepository ballotBoxRepository,
			final ConfigurationEntityStatusService configurationEntityStatusService, final PathResolver pathResolver,
			final KeyStoreService keystoreService, final MixnetInitialPayloadFileRepository mixnetInitialPayloadFileRepository,
			final MixnetShufflePayloadFileRepository mixnetShufflePayloadFileRepository, final ObjectMapper objectMapper,
			@Value("${OR_URL}")
			final String orchestratorUrl,
			@Value("${EI_URL}")
			final String electionInformationUrl,
			@Value("${tenantID}")
			final String tenantId) {
		this.ballotBoxRepository = ballotBoxRepository;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.pathResolver = pathResolver;
		this.keystoreService = keystoreService;
		this.mixnetInitialPayloadFileRepository = mixnetInitialPayloadFileRepository;
		this.mixnetShufflePayloadFileRepository = mixnetShufflePayloadFileRepository;
		this.objectMapper = objectMapper;
		this.orchestratorUrl = orchestratorUrl;
		this.electionInformationUrl = electionInformationUrl;
		this.tenantId = tenantId;
	}

	@PostConstruct
	void configure() {
		final PrivateKey requestSigningKey = keystoreService.getPrivateKey();

		orchestratorRestAdapter = RestClientService.getInstance()
				.getRestClientWithInterceptorAndJacksonConverter(orchestratorUrl, requestSigningKey, "SECURE_DATA_MANAGER");

		electionInformationRestAdapter = RestClientService.getInstance()
				.getRestClientWithInterceptorAndJacksonConverter(electionInformationUrl, requestSigningKey, "SECURE_DATA_MANAGER");
	}

	/**
	 * Downloads a ballot box based on the election event id and the ballot box id.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotBoxId     the ballot box id.
	 * @throws BallotBoxDownloadException if the ballot box to download does not exist in DB or the download/write of the ballot box fails.
	 */
	public void download(final String electionEventId, final String ballotBoxId) throws BallotBoxDownloadException {
		final JsonNode ballotBox = getBallotBoxJson(ballotBoxId, electionEventId);
		checkBallotBoxStatusForDownload(ballotBox, ballotBoxId);

		final ResponseBody responseBody = downloadBallotBox(electionEventId, ballotBoxId);
		final String ballotId = ballotBox.get(JsonConstants.BALLOT).get(JsonConstants.ID).textValue();
		writeBallotBoxStreamToFile(electionEventId, ballotId, ballotBoxId, responseBody);
	}

	/**
	 * Downloads the outputs generated when mixing the requested ballot box on the online control component nodes.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotBoxId     the ballot box id.
	 * @throws BallotBoxDownloadException if the ballot box to download does not exist in DB or the download/write of the ballot box fails.
	 */
	public void downloadPayloads(final String electionEventId, final String ballotBoxId) throws BallotBoxDownloadException {
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);
		validateUUID(electionEventId);
		validateUUID(ballotBoxId);

		LOGGER.info("Requesting payloads for ballot box {}...", ballotBoxId);
		final JsonNode ballotBox = getBallotBoxJson(ballotBoxId, electionEventId);

		// Check that the full ballot box has been mixed.
		checkBallotBoxStatusForDownload(ballotBox, ballotBoxId);
		final String ballotId = ballotBox.get(JsonConstants.BALLOT).get(JsonConstants.ID).textValue();

		// Get payloads, then persist them
		final ResponseBody initialPayload = getInitialPayloads(electionEventId, ballotBoxId);
		persistBallotBoxInitialPayload(electionEventId, ballotId, ballotBoxId, initialPayload);

		final ResponseBody shufflePayloads = getMixingPayloads(electionEventId, ballotBoxId);
		persistBallotBoxShufflePayloads(electionEventId, ballotId, ballotBoxId, shufflePayloads);
	}

	/**
	 * Downloads the ballot box and returns it in a ResponseBody stream to be processed.
	 *
	 * @param electionEventId the election event identifier.
	 * @param ballotBoxId     the ballot box identifier
	 * @throws BallotBoxDownloadException if the download call fails.
	 */
	private ResponseBody downloadBallotBox(final String electionEventId, final String ballotBoxId) throws BallotBoxDownloadException {
		final Response<ResponseBody> execute;
		try {
			execute = getElectionInformationClient().getRawBallotBox(tenantId, electionEventId, ballotBoxId).execute();
		} catch (final IOException e) {
			final String errormessage = String
					.format("Failed to download encrypted BallotBox [electionEvent=%s, ballotBox=%s]: %s", electionEventId, ballotBoxId,
							e.getMessage());
			throw new BallotBoxDownloadException(errormessage, e);
		}

		if (!execute.isSuccessful()) {
			final String errormessage = String
					.format("Failed to download encrypted BallotBox [electionEvent=%s, ballotBox=%s]: %s", electionEventId, ballotBoxId, "500");
			throw new BallotBoxDownloadException(errormessage);
		}

		return execute.body();
	}

	/**
	 * Downloads the initial payload of the mixing process.
	 *
	 * @param electionEventId the election event identifier.
	 * @param ballotBoxId     the ballot box identifier
	 * @throws BallotBoxDownloadException if the download call fails.
	 */
	private ResponseBody getInitialPayloads(final String electionEventId, final String ballotBoxId) throws BallotBoxDownloadException {
		LOGGER.info("Requesting to the Orchestrator the initial payload for ballot box {} ", ballotBoxId);
		final Response<ResponseBody> execute;
		try {
			execute = getOrchestratorClient().getMixnetInitialPayloads(tenantId, electionEventId, ballotBoxId).execute();
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to communicate with orchestrator.", e);
		}

		if (!execute.isSuccessful()) {
			final String errormessage = String
					.format("Failed to download mixnet initial payload [electionEvent=%s, ballotBox=%s]: %s", electionEventId, ballotBoxId, "500");
			throw new BallotBoxDownloadException(errormessage);
		}

		return execute.body();
	}

	/**
	 * Downloads the payloads of the online mixing nodes.
	 *
	 * @param electionEventId the election event identifier.
	 * @param ballotBoxId     the ballot box identifier
	 * @throws BallotBoxDownloadException if the download call fails.
	 */
	private ResponseBody getMixingPayloads(final String electionEventId, final String ballotBoxId) throws BallotBoxDownloadException {
		LOGGER.info("Requesting to the Orchestrator the mixnet shuffle payloads for ballot box {} ", ballotBoxId);

		final Response<ResponseBody> response;
		try {
			response = getOrchestratorClient().getMixnetShufflePayloads(tenantId, electionEventId, ballotBoxId).execute();
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to communicate with orchestrator.", e);
		}

		if (!response.isSuccessful()) {
			final String errorBodyString;
			try {
				errorBodyString = response.errorBody().string();
			} catch (final IOException e) {
				throw new UncheckedIOException("Failed to convert response body to string.", e);
			}
			final String errormessage = String
					.format("Failed to download the mixnet shuffle payloads [electionEvent=%s, ballotBox=%s]: %s", electionEventId, ballotBoxId,
							errorBodyString);
			throw new BallotBoxDownloadException(errormessage);
		}

		return response.body();
	}

	/**
	 * Extracts the ballot box bytes from the stream and write them to file.
	 *
	 * @param responseBody the stream containing the ballot box raw bytes
	 */
	private void writeBallotBoxStreamToFile(final String electionEventId, final String ballotId, final String ballotBoxId, final ResponseBody responseBody)
			throws BallotBoxDownloadException {
		final Path ballotBoxFile;
		try (final InputStream stream = responseBody.byteStream()) {
			ballotBoxFile = getDownloadedBallotBoxPath(electionEventId, ballotId, ballotBoxId);
			Files.copy(stream, ballotBoxFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (final IOException e) {
			final String errorMessage = String
					.format("Failed to write downloaded BallotBox [electionEvent=%s, ballotBox=%s] to file.", electionEventId, ballotBoxId);
			throw new BallotBoxDownloadException(errorMessage, e);
		}
	}

	/**
	 * Stores downloaded initial payload in SDM file system.
	 *
	 * @param payloadResponse the payloads in JSON format.
	 */
	private void persistBallotBoxInitialPayload(final String electionEventId, final String ballotId, final String ballotBoxId, final ResponseBody payloadResponse) {
		LOGGER.info("Persist locally the mixnet initial payloads for ballot box {} ", ballotBoxId);

		final MixnetInitialPayload[] payloads;
		try {
			payloads = objectMapper.readValue(payloadResponse.string(), MixnetInitialPayload[].class);
		} catch (final IOException e) {
			final String errorMessage = String.format("The ballot box's initial payload can not be read, ballotBox: %s", ballotBoxId);
			throw new UncheckedIOException(errorMessage, e);
		}

		// There is at most 1 mixnet initial payload per ballot box.
		if (payloads.length == 1) {
			final MixnetInitialPayload payload = payloads[0];
			mixnetInitialPayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, payload);
			LOGGER.info("Mixnet initial payload stored for electionEvent:{}, ballot:{}, ballotBox:{}", electionEventId, ballotId, ballotBoxId);
		}
	}

	/**
	 * Stores downloaded mixing payloads in SDM file system.
	 *
	 * @param payloadResponse the payloads in JSON format.
	 */
	private void persistBallotBoxShufflePayloads(final String electionEventId, final String ballotId, final String ballotBoxId, final ResponseBody payloadResponse) {
		LOGGER.info("Persist locally the mixnet shuffle payloads for ballot box {} ", ballotBoxId);

		final MixnetShufflePayload[] payloads;
		try {
			payloads = objectMapper.readValue(payloadResponse.string(), MixnetShufflePayload[].class);
		} catch (final IOException e) {
			final String errorMessage = String.format("The ballot box's mixnet shuffle payloads can not be read, ballotBox: %s", ballotBoxId);
			throw new UncheckedIOException(errorMessage, e);
		}

		for (final MixnetShufflePayload payload : payloads) {
			mixnetShufflePayloadFileRepository.savePayload(electionEventId, ballotId, ballotBoxId, payload);
			LOGGER.info("Mixnet shuffle payload stored for electionEvent:{}, ballot:{}, ballotBox:{}", electionEventId, ballotId, ballotBoxId);
		}
	}

	public void updateBallotBoxStatus(final String ballotBoxId) {
		configurationEntityStatusService.update(Status.BB_DOWNLOADED.name(), ballotBoxId, ballotBoxRepository);
	}

	/**
	 * Returns a client for mixed decrypted ballot box in orchestrator context. Used for testing purposes with spy.
	 *
	 * @return the rest client.
	 */
	OrchestratorClient getOrchestratorClient() {
		return orchestratorRestAdapter.create(OrchestratorClient.class);

	}

	/**
	 * Returns a client for the raw ballot box in election information context. Used for testing purposes with spy.
	 *
	 * @return the rest client.
	 */
	ElectionInformationClient getElectionInformationClient() {
		return electionInformationRestAdapter.create(ElectionInformationClient.class);

	}

	/**
	 * Checks if the ballot box has the appropriate status to be downloaded. A ballot box can be downloaded only if it has the status {@link
	 * Status#MIXED}.
	 *
	 * @param ballotBoxJsonNode the JSON representation of the ballot box.
	 * @param ballotBoxId       the ballot box identifier.
	 * @throws BallotBoxDownloadException if the ballot box has not the status {@link Status#MIXED}.
	 */
	private static void checkBallotBoxStatusForDownload(final JsonNode ballotBoxJsonNode, final String ballotBoxId)
			throws BallotBoxDownloadException {
		LOGGER.info("Checking whether the ballot box {} can be downloaded...", ballotBoxId);

		final Status appropriateStatus = Status.MIXED;
		final String ballotBoxStatusName = ballotBoxJsonNode.get(JsonConstants.STATUS).textValue();
		final Status ballotBoxStatus = Status.valueOf(ballotBoxStatusName);
		if (!appropriateStatus.equals(ballotBoxStatus)) {
			throw new BallotBoxDownloadException(
					String.format("The ballot box %s cannot be downloaded because it has not the appropriate status %s (current status is %s).",
							ballotBoxId, appropriateStatus.name(), ballotBoxStatusName));
		}

		LOGGER.info("The ballot box {} can be downloaded.", ballotBoxId);
	}

	/**
	 * Retrieves a ballot box and returns its JSON representation
	 *
	 * @param ballotBoxId     the ballot box identifier
	 * @param electionEventId the election event the ballot box belongs to
	 * @return a JSON representation of the ballot box
	 */
	private JsonNode getBallotBoxJson(final String ballotBoxId, final String electionEventId) throws BallotBoxDownloadException {
		LOGGER.info("Retrieving the JSON representation of ballot box {}...", ballotBoxId);

		// Prepare query
		final Map<String, Object> criteria = new HashMap<>();
		criteria.put(JsonConstants.ID, ballotBoxId);
		criteria.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		final String json = ballotBoxRepository.list(criteria);

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode ballotBoxJsonNode;
		try {
			ballotBoxJsonNode = mapper.readTree(json);
		} catch (final JsonProcessingException e) {
			throw new BallotBoxDownloadException("Parsing error of json ballot box", e);
		}

		final ArrayNode ballotBoxes = (ArrayNode) ballotBoxJsonNode.get(JsonConstants.RESULT);

		if (ballotBoxes.isEmpty()) {
			LOGGER.info("No ballot boxes found for ballotBoxId {}", ballotBoxId);
			throw new BallotBoxDownloadException("Ballot boxes are empty");
		}

		// The JSON structure is an array with a unique ballotBox.
		return ballotBoxes.get(0);
	}

	private Path getDownloadedBallotBoxPath(final String electionEventId, final String ballotId, final String ballotBoxId) {
		return pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, electionEventId, Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS, ballotId, Constants.CONFIG_DIR_NAME_BALLOTBOXES,
				ballotBoxId, Constants.CONFIG_FILE_NAME_ELECTION_INFORMATION_DOWNLOADED_BALLOT_BOX);
	}
}
