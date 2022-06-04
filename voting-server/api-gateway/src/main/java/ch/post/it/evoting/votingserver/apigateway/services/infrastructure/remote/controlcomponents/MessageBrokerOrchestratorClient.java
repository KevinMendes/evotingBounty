/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents;

import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MessageBrokerOrchestratorClient {

	@POST("api/v1/configuration/setupvoting/keygeneration/electionevent/{electionEventId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<JsonArray> generateCCKeys(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Body
			final RequestBody controlComponentKeyGenerationRequestPayload,
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
			final String xForwardedFor,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId);

}