/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.clients;

import javax.validation.constraints.NotNull;

import ch.post.it.evoting.securedatamanager.commons.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface VoteVerificationClient {

	@POST("codesmappingdata/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveCodesMappingData(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_SET_ID_PARAM)
					String verificationCardSetId,
			@Path(Constants.ADMIN_BOARD_ID)
					String adminBoardId, @NotNull
	@Body
			RequestBody body);

	@POST("verificationcarddata/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVerificationCardData(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_SET_ID_PARAM)
					String votingCardSetId,
			@Path(Constants.ADMIN_BOARD_ID)
					String adminBoardId, @NotNull
	@Body
			RequestBody body);

	@POST("verificationcardsetdata/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVerificationCardSetData(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_SET_ID_PARAM)
					String votingCardSetId,
			@Path(Constants.ADMIN_BOARD_ID)
					String adminBoardId, @NotNull
	@Body
			RequestBody body);
}
