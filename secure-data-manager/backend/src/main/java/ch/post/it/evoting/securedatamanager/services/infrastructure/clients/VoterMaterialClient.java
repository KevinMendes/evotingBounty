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

public interface VoterMaterialClient {

	@POST("voterinformationdata/tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveVoterInformationData(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VOTING_CARD_SET_ID)
					String votingCardSetId,
			@Path(Constants.ADMIN_BOARD_ID)
					String adminBoardId, @NotNull
	@Body
			RequestBody body);

	@POST("credentialdata/tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}")
	Call<ResponseBody> saveCredentialData(
			@Path(Constants.TENANT_ID)
					String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VOTING_CARD_SET_ID)
					String votingCardSetId,
			@Path(Constants.ADMIN_BOARD_ID)
					String adminBoardId, @NotNull
	@Body
			RequestBody body);
}
