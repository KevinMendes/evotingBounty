/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.clients;

import java.util.List;

import javax.validation.constraints.NotNull;

import ch.post.it.evoting.securedatamanager.commons.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface OrchestratorClient {

	@POST("choicecodes/computeGenerationContributions")
	Call<ResponseBody> compute(@NotNull
	@Body
			RequestBody body);

	@GET("choicecodes/tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/chunkId/{chunkId}/computeGenerationContributions")
	@Streaming
	Call<ResponseBody> download(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_SET_ID_PARAM)
					String verificationCardSetId,
			@Path(Constants.CHUNK_ID_PARAM)
					int chunkId);

	@GET("choicecodes/tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/generationContributions/status")
	Call<ResponseBody> getChoiceCodesComputationStatus(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_SET_ID_PARAM)
					String verificationCardSetId,
			@Query(Constants.CHUNK_COUNT_PARAM)
					int chunkCount);

	@POST(Constants.BALLOT_BOXES_PATH)
	@Headers("Accept:application/json")
	@Streaming
	Call<ResponseBody> mixBallotBoxes(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Body
					List<String> ballotBoxIds);

	@GET(Constants.BALLOT_BOX_PATH + "/status")
	Call<ResponseBody> getBallotBoxMixingStatus(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.BALLOT_BOX_ID)
					String ballotBoxId);

	@GET(Constants.BALLOT_BOX_PATH + "/mixnetShufflePayloads")
	@Headers("Accept:application/json")
	@Streaming
	Call<ResponseBody> getMixnetShufflePayloads(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.BALLOT_BOX_ID)
					String ballotBoxId);

	@GET(Constants.BALLOT_BOX_PATH + "/mixnetInitialPayloads")
	@Headers("Accept:application/json")
	@Streaming
	Call<ResponseBody> getMixnetInitialPayloads(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.BALLOT_BOX_ID)
					String ballotBoxId);
}
