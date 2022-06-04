/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin;

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
import retrofit2.http.Path;

/**
 * The Interface of vote verification client for admin.
 */
public interface VoteVerificationAdminClient {

	@POST("{pathCodesmappingdata}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveCodesMappingData(
			@Path(Constants.PARAMETER_PATH_CODESMAPPINGDATA)
					String pathCodesmappingdata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId,
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
					RequestBody codesMappings);

	@POST("{pathVerificationcarddata}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVerificationCardData(
			@Path(Constants.PARAMETER_PATH_VERIFICATIONCARDDATA)
					String pathVerificationcarddata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId,
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
					RequestBody verificationCardData);

	@POST("{pathVerificationcardsetdata}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVerificationCardSetData(
			@Path(Constants.PARAMETER_PATH_VERIFICATIONCARDSETDATA)
					String pathVerificationcardsetdata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId,
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
					RequestBody verificationCardSetData);

	@POST("{pathElectoraldata}/tenant/{tenantId}/electionevent/{electionEventId}/electoralauthority/{electoralAuthorityId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveElectoralData(
			@Path(Constants.PARAMETER_PATH_ELECTORALDATA)
					String pathElectoraldata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_ELECTORAL_AUTHORITY_ID)
					String electoralAuthorityId,
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
					RequestBody electoralData);

	@GET("{pathTenantData}/activatetenant/tenant/{tenantId}")
	Call<JsonObject> checkTenantActivation(
			@Path(Constants.PARAMETER_PATH_TENANT_DATA)
					String pathTenantData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@POST("api/v1/configuration/returncodesmappingtable/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<JsonArray> saveReturnCodesMappingTable(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@Body
			final RequestBody returnCodesMappingTablePayload,
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
			final String xForwardedFor,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId);

	@POST("api/v1/configuration/electioncontext/electionevent/{electionEventId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<JsonArray> saveElectionEventContext(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Body
			final RequestBody electionEventContextPayload,
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
			final String xForwardedFor,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId);
}
