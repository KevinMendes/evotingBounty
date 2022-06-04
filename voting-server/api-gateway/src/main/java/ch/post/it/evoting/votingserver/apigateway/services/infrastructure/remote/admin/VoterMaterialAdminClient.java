/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * The Interface of voter material client for admin.
 */
public interface VoterMaterialAdminClient {

	@POST("{pathCredentialdata}/tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveCredentialData(
			@Path(Constants.PARAMETER_PATH_CREDENTIALDATA)
					String pathCredentialdata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_SET_ID)
					String votingCardSetId,
			@Path(Constants.PARAMETER_VALUE_ADMIN_BOARD_ID)
					String adminBoardId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody credentials);

	@POST("{pathVoterInformationData}/tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVoterInformationData(
			@Path(Constants.PARAMETER_PATH_VOTERINFORMATIONDATA)
					String pathVoterInformationData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_SET_ID)
					String votingCardSetId,
			@Path(Constants.PARAMETER_VALUE_ADMIN_BOARD_ID)
					String adminBoardId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody voterInformations);

	@GET("{pathVoterinformation}/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/query")
	Call<JsonObject> getVotingCardsByQuery(
			@Path(Constants.PARAMETER_PATH_VOTERINFORMATION)
					String pathVoterinformation,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Query(Constants.QUERY_PARAMETER_SEARCH_WITH_VOTING_CARD_ID)
					String termVotingCardId,
			@Query(Constants.QUERY_PARAMETER_OFFSET)
					String offset,
			@Query(Constants.QUERY_PARAMETER_SIZE)
					String sizeLimit,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature);
}
