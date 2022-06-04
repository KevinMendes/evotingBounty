/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.voting;

import java.io.IOException;
import java.io.Reader;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.apigateway.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthResponse;
import ch.post.it.evoting.votingserver.apigateway.model.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.apigateway.model.NumberOfRemainingAttempts;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.ExtendedAuthenticationVotingClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import retrofit2.http.Body;

/**
 * The end point for the Extended Authentication feature.
 */
@Stateless(name = "ag-ExtAuthResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(ExtendedAuthenticationResource.RESOURCE_PATH)
public class ExtendedAuthenticationResource {

	/**
	 * The length in characters of the random generated string.
	 */
	public static final int LENGTH_IN_CHARS = 16;
	static final String RESOURCE_PATH = RestApplication.API_OV_VOTING_BASEURI_EXTENDED_AUTH + "/" + RestApplication.PARAMETER_EXTENDED_AUTH_PATH;
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();
	static final String EXT_AUTH_PATH = PROPERTIES.getPropertyValue("EXTENDED_AUTHENTICATION_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthenticationResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final ExtendedAuthenticationVotingClient extendedAuthClient;
	private final TrackIdGenerator trackIdGenerator;
	private final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	ExtendedAuthenticationResource(ExtendedAuthenticationVotingClient extendedAuthenticationVotingClient, TrackIdGenerator trackIdGenerator) {
		this.extendedAuthClient = extendedAuthenticationVotingClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getEncryptedStartVotingKey(
			@PathParam(RestApplication.PARAMETER_VALUE_VERSION)
			final String version,
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final Reader extendedAuthReader,
			@Context
			final HttpServletRequest request) throws IOException {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		ExtendedAuthResponse extendedAuthResponse;
		Response.ResponseBuilder builder;
		try {
			final ExtendedAuthentication extendedAuthentication = objectMapper.readValue(extendedAuthReader, ExtendedAuthentication.class);
			extendedAuthResponse = RetrofitConsumer.processResponse(extendedAuthClient
					.getEncryptedStartVotingKey(EXT_AUTH_PATH, tenantId, electionEventId, xForwardedFor, trackingId, extendedAuthentication));

			Response.Status status = Response.Status.valueOf(extendedAuthResponse.getResponseCode());

			switch (status) {
			case OK:
				builder = Response.ok().entity(new EncryptedSVK(extendedAuthResponse.getEncryptedSVK()));
				break;
			case UNAUTHORIZED:
				builder = Response.ok().entity(new NumberOfRemainingAttempts(extendedAuthResponse.getNumberOfRemainingAttempts()));
				break;
			default:
				builder = Response.status(status);
				break;
			}

			return builder.build();

		} catch (RetrofitException e) {
			LOGGER.error("Error in extended authentication", e);
			return Response.status(e.getHttpCode()).build();
		}
	}

	@PUT
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response updateExtendedAuthData(
			@PathParam(RestApplication.PARAMETER_VALUE_VERSION)
			final String version,
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@HeaderParam(RestApplication.PARAMETER_AUTHENTICATION_TOKEN)
			final String authenticationToken,
			@NotNull
			@Body
			final Reader extendedAuthUpdateReader,
			@Context
			final HttpServletRequest request) throws IOException {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		ExtendedAuthResponse extendedAuthResponse;

		try {
			final ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdate = objectMapper
					.readValue(extendedAuthUpdateReader, ExtendedAuthenticationUpdateRequest.class);
			extendedAuthResponse = RetrofitConsumer.processResponse(extendedAuthClient
					.updateExtendedAuthData(EXT_AUTH_PATH, tenantId, electionEventId, authenticationToken, xForwardedFor, trackingId,
							extendedAuthenticationUpdate));

			Response.Status status = Response.Status.valueOf(extendedAuthResponse.getResponseCode());

			return Response.status(status).build();

		} catch (RetrofitException e) {
			LOGGER.error("Error in extended authentication", e);
			return Response.status(e.getHttpCode()).build();
		}
	}
}
