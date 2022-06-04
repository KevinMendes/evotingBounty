/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote.admin;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * The Interface VoterMaterialAdminClient.
 */
public interface VoterMaterialAdminClient {

	/**
	 * Gets the voter informations.
	 *
	 * @param pathVoterinformation the path voterinformation
	 * @param tenantId             the tenant id
	 * @param electionEventId      the election event id
	 * @param votingCardId         the voting card id
	 * @return the voter informations
	 */
	@GET("{pathVoterinformation}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<VoterInformation> getVoterInformations(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_VOTERINFORMATION)
					String pathVoterinformation,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId) throws ResourceNotFoundException;
}
