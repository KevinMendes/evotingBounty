/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Defines the methods to access via REST to a set of operations.
 */
public interface VoterMaterialClient {

	/**
	 * The endpoint to get credential data from voter material context.
	 *
	 * @param requestId       - the request id for logging purposes.
	 * @param pathMaterials   the path materials
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param credentialId    - the credential id
	 * @return the credentials for a given tenant, election event and voting card.
	 * @throws ResourceNotFoundException if the rest operation fails.
	 */
	@GET("{pathMaterials}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<Credential> getCredential(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String requestId,
			@Path(Constants.PATH_MATERIALS)
					String pathMaterials,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId) throws ResourceNotFoundException;

	/**
	 * The endpoint to get voter information data from voter material context.
	 *
	 * @param requestId        - the request id for logging purposes.
	 * @param pathInformations - the path to the correspoding endpoint.
	 * @param tenantId         - the tenant id
	 * @param electionEventId  - the election event id
	 * @param credentialId     - the credential id
	 * @return the voter information for a given tenant, election event and voting card.
	 * @throws ResourceNotFoundException if the rest operation fails.
	 */
	@GET("{pathInformations}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<VoterInformation> getVoterInformation(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String requestId,
			@Path(Constants.PARAMETER_PATH_INFORMATIONS)
					String pathInformations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId) throws ResourceNotFoundException;
}
