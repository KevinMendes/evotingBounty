/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.actions.ValidateExtendedAuthUpdateAction;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationRepository;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.AuthenticationException;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationService;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationServiceImpl;

/**
 * Web service for querying for extended authentication.
 */
@Path(ExtendedAuthenticationResource.RESOURCE_PATH)
@Stateless
public class ExtendedAuthenticationResource {

	public static final String RESOURCE_PATH = "/extendedauthentication/authenticate";

	public static final String AUTHENTICATE_PATH = "/tenant/{tenantId}/electionevent/{electionevent}";

	public static final String UPDATE_PATH = "/tenant/{tenantId}/electionevent/{electionevent}";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT = "electionevent";

	// The name of the parameter value authentication token.
	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthenticationResource.class);

	@EJB
	protected ExtendedAuthenticationRepository extendedAuthenticationRepository;

	@Inject
	protected ExtendedAuthenticationService extendedAuthenticationService;

	@Inject
	protected TrackIdInstance trackIdInstance;

	@EJB
	protected ValidateExtendedAuthUpdateAction validateExtendedAuthUpdateAction;

	@Path(ExtendedAuthenticationResource.AUTHENTICATE_PATH)
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)

	public Response authenticate(
			@NotNull
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@NotNull
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT)
					String electionEvent,
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@NotNull
					ExtendedAuthentication extendedAuthentication,
			@Context
					HttpServletRequest request) throws GeneralCryptoLibException {

		ExtendedAuthResponse.Builder builder = new ExtendedAuthResponse.Builder();
		builder.setNumberOfRemainingAttempts(0);
		trackIdInstance.setTrackId(trackId);
		try {
			EncryptedSVK encryptedStartVotingKey = extendedAuthenticationService
					.authenticate(tenantId, extendedAuthentication.getAuthId(), extendedAuthentication.getExtraParam(), electionEvent);
			builder.setEncryptedSVK(encryptedStartVotingKey.getEncryptedSVK());
			builder.setResponseCode(Status.OK);

		} catch (ResourceNotFoundException e) {
			builder.setResponseCode(Status.NOT_FOUND);
			LOGGER.error("AuthId not found" + createErrorDetails(tenantId, electionEvent, extendedAuthentication.getAuthId()), e);
		} catch (AllowedAttemptsExceededException e) {
			builder.setResponseCode(Status.FORBIDDEN);
			LOGGER.error("Allowed maximum number of (" + ExtendedAuthenticationServiceImpl.MAX_ALLOWED_NUMBER_OF_ATTEMPTS + ") attempts exceeded"
					+ createErrorDetails(tenantId, electionEvent, extendedAuthentication.getAuthId()), e);
		} catch (AuthenticationException e) {
			if (e.getRemainingAttempts() == 0) {
				builder.setResponseCode(Status.FORBIDDEN);
			} else {
				builder.setResponseCode(Status.UNAUTHORIZED);
				builder.setNumberOfRemainingAttempts(e.getRemainingAttempts());
			}
			LOGGER.error("Extra parameter based authentication exception" + createErrorDetails(tenantId, electionEvent,
					extendedAuthentication.getAuthId()), e);
		} catch (ApplicationException e) {
			builder.setResponseCode(Status.NOT_FOUND);
			LOGGER.error("AuthId error " + createErrorDetails(tenantId, electionEvent, extendedAuthentication.getAuthId()), e);
		}
		return Response.ok().entity(builder.build()).build();
	}

	private String createErrorDetails(String tenantId, String electionEvent, String authId) {
		StringBuilder sb = new StringBuilder();
		sb.append(" (Tenant Id: ");
		sb.append(tenantId);
		sb.append(", Election event: ");
		sb.append(electionEvent);
		sb.append(", Auth Id: ");
		sb.append(authId);
		sb.append(").");
		return sb.toString();
	}

	@Path(ExtendedAuthenticationResource.UPDATE_PATH)
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(
			@NotNull
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@NotNull
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT)
					String electionEvent,
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@NotNull
			@HeaderParam(PARAMETER_AUTHENTICATION_TOKEN)
					String authenticationToken,
			@NotNull
					ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest,
			@Context
					HttpServletRequest request) {

		ExtendedAuthResponse.Builder builder = new ExtendedAuthResponse.Builder();
		String oldAuthID = "";
		try {
			trackIdInstance.setTrackId(trackId);

			final AuthenticationToken token = DomainObjectMapper.getNewInstance().readValue(authenticationToken, AuthenticationToken.class);

			ExtendedAuthenticationUpdate extendedAuthenticationUpdate = validateExtendedAuthUpdateAction
					.validate(tenantId, electionEvent, token, extendedAuthenticationUpdateRequest);

			oldAuthID = extendedAuthenticationUpdate.getOldAuthID();
			final ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication extendedAuthentication = extendedAuthenticationService
					.renewExistingExtendedAuthentication(tenantId, oldAuthID, extendedAuthenticationUpdate.getNewAuthID(),
							extendedAuthenticationUpdate.getNewSVK(), electionEvent);

			builder.setResponseCode(Status.OK);
			builder.setEncryptedSVK(extendedAuthentication.getEncryptedStartVotingKey());
			builder.setNumberOfRemainingAttempts(extendedAuthentication.getAttempts());

		} catch (ExtendedAuthValidationException e) {
			builder.setResponseCode(Status.BAD_REQUEST);
			LOGGER.error(String.format("Error validating the token + %s", e.getErrorType().name()), e);
		} catch (AuthTokenValidationException e) {
			builder.setResponseCode(Status.BAD_REQUEST);
			LOGGER.error(String.format("Error validating the request +  %s", e.getErrorType().name()), e);
		} catch (ResourceNotFoundException e) {
			builder.setResponseCode(Status.NOT_FOUND);
			LOGGER.error(String.format("AuthId not found + %s", createErrorDetails(tenantId, electionEvent, oldAuthID)), e);
		} catch (ApplicationException e) {
			builder.setResponseCode(Status.CONFLICT);
			LOGGER.error(String.format("AuthId error %s", createErrorDetails(tenantId, electionEvent, oldAuthID)), e);
		} catch (IOException e) {
			builder.setResponseCode(Status.CONFLICT);
			LOGGER.error(String.format("error parsing auth token error %s", createErrorDetails(tenantId, electionEvent, authenticationToken)), e);
		}
		return Response.ok().entity(builder.build()).build();
	}
}
