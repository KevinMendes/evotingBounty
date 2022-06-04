/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client;

import javax.validation.constraints.NotNull;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationData;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * The Interface of authentication client for admin.
 */
public interface AuthenticationClient {

	@GET("{pathAuthenticationData}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<AuthenticationData> getAuthenticationData(
			@Path(Constants.PARAMETER_PATH_AUTHENTICATION_DATA)
					String pathBallotboxstatus,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId);

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
					String authenticationToken);

	@POST("tokens/tenant/{tenantId}/electionevent/{electionEventId}/chain/validate")
	Call<ValidationResult> validateCertificateChain(
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			@Body
			final CertificateChainValidationRequest certificateChainValidationRequest);
}
