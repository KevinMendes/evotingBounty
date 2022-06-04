/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.clients;

import java.util.List;

import javax.ws.rs.core.MediaType;

import ch.post.it.evoting.domain.configuration.ControlComponentKeyGenerationRequestPayload;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MessageBrokerOrchestratorClient {

	@POST("api/v1/configuration/setupvoting/keygeneration/electionevent/{electionEventId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<List<ControlComponentPublicKeysPayload>> generateCCKeys(
			@Path(Constants.ELECTION_EVENT_ID)
			final String electionEventId,
			@Body
			final ControlComponentKeyGenerationRequestPayload controlComponentKeyGenerationRequestPayload);

}
