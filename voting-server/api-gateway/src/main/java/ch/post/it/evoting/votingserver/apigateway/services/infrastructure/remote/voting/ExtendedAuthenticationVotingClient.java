/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting;

import javax.validation.constraints.NotNull;

import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthResponse;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * The Interface of extended authentication client for voting.
 */
public interface ExtendedAuthenticationVotingClient {

	@POST("{extAuthPath}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ExtendedAuthResponse> getEncryptedStartVotingKey(
			@Path(value = Constants.PARAMETER_PATH_EXT_AUTH, encoded = true)
					String extAuthPath,
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
			@Body
					ExtendedAuthentication extendedAuthentication);

	@PUT("{extAuthPath}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ExtendedAuthResponse> updateExtendedAuthData(
			@Path(value = Constants.PARAMETER_PATH_EXT_AUTH, encoded = true)
					String extAuthPath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
			final String authenticationToken,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdate);

	@POST("{extAuthPath}/tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveExtendedAuthentication(
			@Path(value = Constants.PARAMETER_PATH_EXT_AUTH, encoded = true)
					String extAuthPath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Body
					RequestBody extendedAuth);

	@GET("{extAuthPath}/tenant/{tenantId}/electionevent/{electionEventId}/blocked")
	@Streaming
	Call<ResponseBody> getBlockedExtendedAuthentications(
			@Path(value = Constants.PARAMETER_PATH_EXT_AUTH, encoded = true)
					String extAuthPath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);
}
