/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.configuration;

import java.io.InputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;

import jakarta.validation.constraints.NotNull;
import okhttp3.RequestBody;

/**
 * Web service for handling election event context resources
 */
@Stateless(name = "ag-ElectionEventContextResource")
@Path("/vv")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ElectionEventContextResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextResource.class);

	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();

	@Inject
	private TrackIdGenerator trackIdGenerator;

	@Inject
	private VoteVerificationAdminClient voteVerificationAdminClient;

	@POST
	@Path("api/v1/configuration/electioncontext/electionevent/{electionEventId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveElectionEventContext(
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final InputStream electionEventContextPayload,
			@HeaderParam(RestClientInterceptor.HEADER_ORIGINATOR)
			final String originator,
			@HeaderParam(RestClientInterceptor.HEADER_SIGNATURE)
			final String signature,
			@Context
			final HttpServletRequest request) {

		final String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		final String trackingId = trackIdGenerator.generate();
		final RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, electionEventContextPayload);

		JsonArray response;
		try {

			response = RetrofitConsumer.processResponse(
					voteVerificationAdminClient.saveElectionEventContext(electionEventId, body, originator, signature, xForwardedFor, trackingId));
			return Response.ok().entity(response.toString()).build();
		} catch (final RetrofitException e) {
			LOGGER.error("Error trying request control component election context.", e);
			return Response.status(e.getHttpCode()).build();
		}
	}

}