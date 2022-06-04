/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface OrchestratorClient {

	@POST("{pathComputeChoiceCodesRequest}/computeGenerationContributions")
	Call<ResponseBody> requestComputeChoiceCodes(
			@Path(Constants.PARAMETER_PATH_COMPUTE_CHOICE_CODES_REQUEST)
					String pathComputeChoiceCodes,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody computeInputJsonString);

	@GET("{pathComputeChoiceCodesRetrieval}/tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/chunkId/{chunkId}/computeGenerationContributions")
	Call<ResponseBody> retrieveComputedChoiceCodes(
			@Path(Constants.PARAMETER_PATH_COMPUTE_CHOICE_CODES_RETRIEVAL)
					String pathComputeChoiceCodes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId,
			@Path(Constants.PARAMETER_VALUE_CHUNK_ID)
					int chunkId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@POST("{pathMixDecrypt}/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotboxes")
	Call<ResponseBody> processBallotBoxes(
			@Path(Constants.PARAMETER_PATH_MIX_DECRYPT)
					String pathMixDecrypt,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
			@Body
					List<String> ballotBoxIds,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathMixDecrypt}/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/mixnetInitialPayloads")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	@Streaming
	Call<ResponseBody> getMixnetInitialPayloads(
			@Path(Constants.PARAMETER_PATH_MIX_DECRYPT)
					String pathMixDecrypt,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathMixDecrypt}/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/mixnetShufflePayloads")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	@Streaming
	Call<ResponseBody> getMixnetShufflePayloads(
			@Path(Constants.PARAMETER_PATH_MIX_DECRYPT)
					String pathMixDecrypt,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathMixDecrypt}/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/status")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<JsonObject> getBallotBoxStatus(
			@Path(Constants.PARAMETER_PATH_MIX_DECRYPT)
					String pathMixDecrypt,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathComputeChoiceCodes}/tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/generationContributions/status")
	Call<JsonObject> getChoiceCodesComputationStatus(
			@Path(Constants.PARAMETER_PATH_COMPUTE_CHOICE_CODES)
					String pathComputeChoiceCodes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId,
			@Query(Constants.PARAMETER_VALUE_CHUNK_COUNT)
					int chunkCount,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);
}
