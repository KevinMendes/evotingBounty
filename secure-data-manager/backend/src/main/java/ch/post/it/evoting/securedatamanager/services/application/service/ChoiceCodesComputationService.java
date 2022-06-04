/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.post.it.evoting.securedatamanager.services.application.exception.ChoiceCodesComputationServiceException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.OrchestratorClient;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class ChoiceCodesComputationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceCodesComputationService.class);

	@Autowired
	private VotingCardSetRepository votingCardSetRepository;

	@Autowired
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	@Value("${OR_URL}")
	private String orchestratorUrl;

	@Value("${tenantID}")
	private String tenantId;

	/**
	 * Check if the choice code contributions for generation are ready and update the status of the voting card set they belong to.
	 */
	public void updateChoiceCodesComputationStatus(final String electionEventId) throws IOException {
		final Map<String, Object> votingCardSetsParams = new HashMap<>();
		votingCardSetsParams.put(JsonConstants.STATUS, Status.COMPUTING.name());
		votingCardSetsParams.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);

		final String votingCardSetJSON = votingCardSetRepository.list(votingCardSetsParams);
		final JsonArray votingCardSets = new JsonParser().parse(votingCardSetJSON).getAsJsonObject().get(JsonConstants.RESULT).getAsJsonArray();

		for (int i = 0; i < votingCardSets.size(); i++) {
			final JsonObject votingCardSetInArray = votingCardSets.get(i).getAsJsonObject();
			final String verificationCardSetId = votingCardSetInArray.get(JsonConstants.VERIFICATION_CARD_SET_ID).getAsString();

			final int chunkCount;
			try {
				chunkCount = returnCodeGenerationRequestPayloadRepository.getCount(electionEventId, verificationCardSetId);
			} catch (final PayloadStorageException e) {
				throw new IllegalStateException(e);
			}

			final Response<ResponseBody> choiceCodesComputationStatusResponse = executeCall(
					getOrchestratorClient().getChoiceCodesComputationStatus(tenantId, electionEventId, verificationCardSetId, chunkCount));

			if (!choiceCodesComputationStatusResponse.isSuccessful()) {
				final String errorBodyString;
				try {
					errorBodyString = choiceCodesComputationStatusResponse.errorBody().string();
				} catch (final IOException e) {
					throw new UncheckedIOException("Failed to convert response body to string.", e);
				}
				throw new ChoiceCodesComputationServiceException(String.format("Request to orchestrator failed with error: %s", errorBodyString));
			}

			final JsonObject jsonObject;
			try (final ResponseBody body = choiceCodesComputationStatusResponse.body(); final Reader reader = body.charStream()) {
				jsonObject = new JsonParser().parse(reader).getAsJsonObject();
			}

			votingCardSetInArray.addProperty(JsonConstants.STATUS, jsonObject.get("status").getAsString());
			votingCardSetRepository.update(votingCardSetInArray.toString());
		}
	}

	/**
	 * Gets the orchestrator Retrofit client
	 */
	private OrchestratorClient getOrchestratorClient() {
		final Retrofit restAdapter = RestClientService.getInstance().getRestClientWithJacksonConverter(orchestratorUrl);
		return restAdapter.create(OrchestratorClient.class);
	}

	/**
	 * Testing-gate for static method mocking.
	 */
	<T> Response<T> executeCall(final Call<T> call) throws IOException {
		return call.execute();
	}
}
