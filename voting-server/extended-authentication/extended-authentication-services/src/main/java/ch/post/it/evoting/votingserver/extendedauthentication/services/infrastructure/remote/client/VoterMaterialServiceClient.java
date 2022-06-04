/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client;

import javax.validation.constraints.NotNull;

import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface VoterMaterialServiceClient {

	@GET("{pathVoterInformationData}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<VoterInformation> getVoterInformationByCredentialId(
			@Path(value = Constants.PARAMETER_PATH_VOTERINFORMATIONDATA, encoded = true)
					String pathVoterInformationData,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId);
}
