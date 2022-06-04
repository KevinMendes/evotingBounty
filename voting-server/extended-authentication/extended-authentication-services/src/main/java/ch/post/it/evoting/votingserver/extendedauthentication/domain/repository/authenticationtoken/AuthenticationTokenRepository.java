/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.repository.authenticationtoken;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.google.gson.Gson;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenRepositoryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.AuthenticationTokenService;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;

/**
 * Implementation of the service. All the things regarding token validations will be redirected to
 * the Context responsible of such operations. Thus, the authentication context will perform any
 * operation regarding auth token validations.
 */
@Stateless
public class AuthenticationTokenRepository implements AuthenticationTokenService {

	// The path to the resource for validate the authentication token.
	private static final String VALIDATION_AUTHENTICATION_TOKEN_PATH = "validations";

	private AuthenticationClient authenticationClient;

	@Inject
	private TrackIdInstance trackId;

	@Inject
	AuthenticationTokenRepository(AuthenticationClient authenticationClient) {
		this.authenticationClient = authenticationClient;
	}

	@Override
	public ValidationResult validateToken(String tenantId, String electionEventId, AuthenticationToken token) throws AuthTokenRepositoryException {

		final String votingCardId = token.getVoterInformation().getVotingCardId();
		Gson gson = new Gson();
		final String authTokenAsString = gson.toJson(token, AuthenticationToken.class);
		try {
			return RetrofitConsumer.processResponse(authenticationClient
					.validateAuthenticationToken(VALIDATION_AUTHENTICATION_TOKEN_PATH, tenantId, electionEventId, votingCardId, trackId.getTrackId(),
							authTokenAsString));
		} catch (RetrofitException rfE) {
			throw new AuthTokenRepositoryException("Error trying to validate token.", rfE);
		}
	}
}
