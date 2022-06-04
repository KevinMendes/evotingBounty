/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.securedatamanager.services.application.exception.VotingCardSetChoiceCodesServiceException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.RestClientService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.clients.OrchestratorClient;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

@Service
public class VotingCardSetChoiceCodesService {

	private final String orchestratorUrl;
	private final String tenantId;
	private final ObjectMapper mapper;

	@Autowired
	public VotingCardSetChoiceCodesService(
			@Value("${OR_URL}")
			final String orchestratorUrl,
			@Value("${tenantID}")
			final String tenantId, final ObjectMapper mapper) {

		this.orchestratorUrl = orchestratorUrl;
		this.tenantId = tenantId;
		this.mapper = mapper;
	}

	/**
	 * Send the necessary input data to the endpoint to compute the choice return codes and the vote cast return code
	 */
	public void sendToCompute(final ReturnCodeGenerationRequestPayload payload) throws IOException {

		// Encapsulate the payload into a typed input stream.
		final byte[] inputBytes = mapper.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8);
		final RequestBody requestBody = RequestBody.create(okhttp3.MediaType.parse(MediaType.APPLICATION_JSON), inputBytes);

		final Response<ResponseBody> response = getOrchestratorClient().compute(requestBody).execute();

		if (!response.isSuccessful()) {
			final String errorBodyString;
			try {
				errorBodyString = response.errorBody().string();
			} catch (final IOException e) {
				throw new UncheckedIOException("Failed to convert response body to string.", e);
			}
			throw new VotingCardSetChoiceCodesServiceException(String.format("Failed to compute voting card set choice codes: %s", errorBodyString));
		}
	}

	/**
	 * Download the contributions of the control components to generate the choice return codes and the vote cast return code
	 */
	public InputStream download(final String electionEventId, final String verificationCardSetId, final int chunkId) throws IOException {
		final Response<ResponseBody> response = getOrchestratorClient().download(tenantId, electionEventId, verificationCardSetId, chunkId).execute();

		if (!response.isSuccessful()) {
			final String errorBodyString;
			try {
				errorBodyString = response.errorBody().string();
			} catch (final IOException e) {
				throw new UncheckedIOException("Failed to convert response body to string.", e);
			}
			throw new VotingCardSetChoiceCodesServiceException(String.format("Failed to download node contributions: %s", errorBodyString));
		}

		return response.body().byteStream();
	}

	/**
	 * Get the orchestrator Retrofit client
	 */
	private OrchestratorClient getOrchestratorClient() {
		final Retrofit restAdapter = RestClientService.getInstance().getRestClientWithJacksonConverter(orchestratorUrl);
		return restAdapter.create(OrchestratorClient.class);
	}
}
