/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.securedatamanager.services.application.exception.BallotBoxServiceException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.OrchestratorClient;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Provides methods dealing with the ballot box mixing.
 */
@Service
public class BallotBoxMixingService {

	private final String tenantId;
	private final String orchestratorUrl;
	private final BallotBoxRepository ballotBoxRepository;
	private final KeyStoreService keystoreService;

	@Autowired
	public BallotBoxMixingService(final BallotBoxRepository ballotBoxRepository, final KeyStoreService keystoreService,
			@Value("${tenantID:100}")
			final String tenantId,
			@Value("${OR_URL}")
			final String orchestratorUrl) {

		this.ballotBoxRepository = ballotBoxRepository;
		this.keystoreService = keystoreService;
		this.tenantId = tenantId;
		this.orchestratorUrl = orchestratorUrl;
	}

	/**
	 * Makes a request (through the api-gateway) to the orchestrator to mix the given ballot boxes.
	 *
	 * @param electionEventId the election event id. Must be non-null.
	 * @param ballotBoxIds    the ids of ballot boxes to mix. Must be non-null and non-empty.
	 * @return the ballot boxes status as a json string.
	 */
	public String mixBallotBoxes(final String electionEventId, final List<String> ballotBoxIds) {
		validateUUID(electionEventId);
		checkNotNull(ballotBoxIds);
		checkArgument(!ballotBoxIds.isEmpty(), "Ballot box ids to mix must not be empty.");
		ballotBoxIds.forEach(UUIDValidations::validateUUID);

		final Response<ResponseBody> response;
		try {
			response = getOrchestratorClient().mixBallotBoxes(tenantId, electionEventId, ballotBoxIds).execute();
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
					.format("Failed to mix ballot boxes [electionEvent=%s, ballotBoxes=%s]: %s", electionEventId, ballotBoxIds, errorBodyString);
			throw new BallotBoxServiceException(errormessage);
		}

		try {
			return response.body().string();
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to convert response body to string.", e);
		}
	}

	/**
	 * Checks if ballot boxes are mixed and updates their status in the database.
	 *
	 * @param electionEventId the election event id. Must be non-null.
	 */
	public void updateBallotBoxesMixingStatus(final String electionEventId) throws IOException {
		validateUUID(electionEventId);

		final Map<String, Object> ballotBoxesParams = new HashMap<>();
		ballotBoxesParams.put(JsonConstants.STATUS, Status.SIGNED.name());
		ballotBoxesParams.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);

		final String ballotBoxJSON = ballotBoxRepository.list(ballotBoxesParams);
		final JsonArray ballotBoxes = new JsonParser().parse(ballotBoxJSON).getAsJsonObject().get(JsonConstants.RESULT).getAsJsonArray();

		final OrchestratorClient client = getOrchestratorClient();

		for (int i = 0; i < ballotBoxes.size(); i++) {
			final JsonObject ballotBoxInArray = ballotBoxes.get(i).getAsJsonObject();
			final String ballotBoxId = ballotBoxInArray.get(JsonConstants.ID).getAsString();

			final Response<ResponseBody> ballotBoxMixingStatusResponse = client.getBallotBoxMixingStatus(tenantId, electionEventId, ballotBoxId)
					.execute();
			if (!ballotBoxMixingStatusResponse.isSuccessful()) {
				final String errorBodyString;
				try {
					errorBodyString = ballotBoxMixingStatusResponse.errorBody().string();
				} catch (final IOException e) {
					throw new UncheckedIOException("Failed to convert response body to string.", e);
				}
				throw new IOException(String.format("Request to orchestrator failed with error: %s", errorBodyString));
			}

			final JsonObject jsonObject;
			try (final ResponseBody body = ballotBoxMixingStatusResponse.body(); final Reader reader = body.charStream()) {
				jsonObject = new JsonParser().parse(reader).getAsJsonObject();
			}

			// Only update the status if it is "MIXED", the other possible mixing
			// statuses are not handled by the Secure Data Manager
			final String mixedStatusValue = Status.MIXED.toString();
			if (jsonObject.get("status") != null && mixedStatusValue.equals(jsonObject.get("status").getAsString())) {
				ballotBoxInArray.addProperty(JsonConstants.STATUS, mixedStatusValue);
				ballotBoxRepository.update(ballotBoxInArray.toString());
			}
		}
	}

	/**
	 * Gets the orchestrator Retrofit client
	 */
	private OrchestratorClient getOrchestratorClient() {
		final PrivateKey privateKey = keystoreService.getPrivateKey();
		final Retrofit restAdapter = RestClientService.getInstance()
				.getRestClientWithInterceptorAndJacksonConverter(orchestratorUrl, privateKey, "SECURE_DATA_MANAGER");
		return restAdapter.create(OrchestratorClient.class);
	}

}
