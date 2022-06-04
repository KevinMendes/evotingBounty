/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * The Interface of authentication client for admin.
 */
public interface AuthenticationAdminClient {

	@POST("{pathElectioneventdata}/tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveElectionEventData(
			@Path(Constants.PARAMETER_PATH_ELECTIONEVENTDATA)
					String pathElectioneventdata,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_PATH_ELECTION_EVENT_ID)
					String electionEventId,
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
					RequestBody electioneventData);

	@GET("{pathElectioneventdata}/tenant/{tenantId}/electionevent/{electionEventId}/status")
	Call<JsonObject> checkIfElectionEventDataIsEmpty(
			@Path(Constants.PARAMETER_PATH_ELECTIONEVENTDATA)
					String pathBallotboxstatus,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_PATH_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

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
}
