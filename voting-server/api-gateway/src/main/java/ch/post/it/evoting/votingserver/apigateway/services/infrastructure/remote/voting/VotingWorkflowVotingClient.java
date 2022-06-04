/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
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
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * The interface of the voting workflow client for voting.
 */
public interface VotingWorkflowVotingClient {

	@GET("{pathInformations}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<JsonObject> findInformationsByTenantElectionEventCredential(
			@Path(Constants.PARAMETER_PATH_INFORMATIONS)
					String pathInformations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@POST("{pathTokens}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<JsonObject> getAuthenticationToken(
			@Path(Constants.PARAMETER_PATH_TOKENS)
					String pathTokens,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody challengeInformationJsonString);

	@POST("{pathConfirmations}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<JsonObject> validateConfirmationMessage(
			@Path(Constants.PARAMETER_PATH_CONFIRMATIONS)
					String pathConfirmations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody confirmationInformationJsonString);

	@POST("{pathVotes}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<JsonObject> validateVoteAndStore(
			@Path(Constants.PARAMETER_PATH_VOTES)
					String pathVotes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody voteJsonString);

	@GET("{pathChoicecodes}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<JsonObject> getChoiceCodes(
			@Path(Constants.PARAMETER_PATH_CHOICECODES)
					String pathChoicecodes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathCastcodes}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<JsonObject> getCastCodeMessage(
			@Path(Constants.PARAMETER_PATH_CASTCODES)
					String pathCastcodes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@NotNull
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String authenticationTokenJsonString,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@POST("{pathVotes}/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/states")
	Call<JsonArray> getStatusOfVotingCards(
			@Path(Constants.PARAMETER_PATH_VOTES)
					String pathVotes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
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
					String signature,
			@NotNull
			@Body
					RequestBody votingCardsJsonString);

	@GET("{pathVotes}/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/states/inactive")
	@Headers("Accept:" + MediaType.APPLICATION_OCTET_STREAM)
	@Streaming
	Call<ResponseBody> getIdAndStateOfInactiveVotingCards(
			@Path(Constants.PARAMETER_PATH_VOTES)
					String pathVotes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
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

	@PUT("{pathVotes}/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/block")
	Call<JsonArray> blockVotingCards(
			@Path(Constants.PARAMETER_PATH_VOTES)
					String pathVotes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
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
					String signature,
			@NotNull
			@Body
					RequestBody blockVotingCardsJsonString);

}
