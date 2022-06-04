/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenMessage;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * The Interface of authentication client.
 */
public interface AuthenticationClient {

	/**
	 * Gets the authentication token message.
	 *
	 * @param pathTokens           the path tokens
	 * @param tenantId             the tenant id
	 * @param electionEventId      the election event id
	 * @param credentialId         the credential id
	 * @param trackId              the track id
	 * @param challengeInformation the challenge information
	 * @return the authentication token
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@POST("{pathTokens}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<AuthenticationTokenMessage> getAuthenticationToken(
			@Path(Constants.PARAMETER_PATH_TOKENS)
					String pathTokens,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Body
					ChallengeInformation challengeInformation) throws ResourceNotFoundException;

	/**
	 * Validate authentication token.
	 *
	 * @param pathValidationTokens the path validation tokens
	 * @param tenantId             the tenant id
	 * @param electionEventId      the election event id
	 * @param votingCardId         the voting card id
	 * @param trackId              the track id
	 * @param authenticationToken  the authentication token
	 * @return the validation result
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@POST("{pathValidationTokens}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<ValidationResult> validateAuthenticationToken(
			@Path(Constants.PARAMETER_PATH_VALIDATION_TOKENS)
					String pathValidationTokens,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String authenticationToken) throws ResourceNotFoundException;

	/**
	 * Find by tenant election event voting card.
	 *
	 * @param trackId          the track id
	 * @param pathInformations the path informations
	 * @param tenantId         the tenant id
	 * @param electionEventId  the election event id
	 * @param credentialId     the credential id
	 * @return the credential information
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathInformations}/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	Call<CredentialInformation> findByTenantElectionEventCredential(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_INFORMATIONS)
					String pathInformations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId) throws ResourceNotFoundException;

	/**
	 * Find by tenant and election event.
	 *
	 * @param trackId          the track id
	 * @param pathCertificates the path certificates
	 * @param tenantId         the tenant id
	 * @param electionEventId  the election event id
	 * @return the credential information
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathCertificates}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<AdminBoardCertificates> findByTenantElectionEventCertificates(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_CERTIFICATES)
					String pathCertificates,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId) throws ResourceNotFoundException;
}
