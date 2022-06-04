/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.compute;

import java.io.InputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents.OrchestratorClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Web service to handle the computation (generation) of the choice return codes in the control components
 */
@Stateless(name = "ag-ComputeResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(ComputeResource.RESOURCE_PATH)
public class ComputeResource {

	static final String RESOURCE_PATH = "/or/choicecodes";
	static final String COMPUTE_CHOICE_CODES_REQUEST = "computeGenerationContributions";
	static final String COMPUTE_CHOICE_CODES_RETRIEVAL = "tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/chunkId/{chunkId}/computeGenerationContributions";
	static final String CHECK_COMPUTATION_STATUS = "tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/generationContributions/status";
	static final String COMPUTE_PATH = PropertiesFileReader.getInstance().getPropertyValue("COMPUTE_CHOICE_CODES_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(ComputeResource.class);

	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final TrackIdGenerator trackIdGenerator;
	private final OrchestratorClient orchestratorClient;

	@Inject
	ComputeResource(OrchestratorClient orchestratorClient, TrackIdGenerator trackIdGenerator) {
		this.trackIdGenerator = trackIdGenerator;
		this.orchestratorClient = orchestratorClient;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path(COMPUTE_CHOICE_CODES_REQUEST)
	public Response compute(
			@NotNull
			final InputStream computeInput,
			@Context
			final HttpServletRequest request) {

		LOGGER.info("Received compute request.");

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		final RequestBody computeInputJson = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, computeInput);
		LOGGER.info("RequestBody created. Sending request to {}.", COMPUTE_PATH);

		try (ResponseBody responseBody = RetrofitConsumer
				.processResponse(orchestratorClient.requestComputeChoiceCodes(COMPUTE_PATH, xForwardedFor, trackingId, computeInputJson))) {
			return Response.ok().build();
		} catch (RetrofitException e) {
			LOGGER.error("Error trying to compute choice codes.", e);
			return Response.status(e.getHttpCode()).build();
		}
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Path(COMPUTE_CHOICE_CODES_RETRIEVAL)
	public Response compute(
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(RestApplication.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@PathParam(RestApplication.PARAMETER_VALUE_CHUNK_ID)
			final int chunkId,
			@Context
			final HttpServletRequest request) {

		final String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		final String trackingId = trackIdGenerator.generate();

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(orchestratorClient
				.retrieveComputedChoiceCodes(COMPUTE_PATH, tenantId, electionEventId, verificationCardSetId, chunkId, xForwardedFor, trackingId))) {
			return Response.ok().entity(responseBody.byteStream()).build();
		} catch (RetrofitException e) {
			LOGGER.error("Error trying to compute choice codes.", e);
			return Response.status(e.getHttpCode()).build();
		}
	}

	@GET
	@Path(CHECK_COMPUTATION_STATUS)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkComputationStatus(
			@PathParam(RestApplication.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(RestApplication.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(RestApplication.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@QueryParam(RestApplication.PARAMETER_VALUE_CHUNK_COUNT)
			final int chunkCount,
			@Context
			final HttpServletRequest request) {

		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();
		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(orchestratorClient
					.getChoiceCodesComputationStatus(COMPUTE_PATH, tenantId, electionEventId, verificationCardSetId, chunkCount, xForwardedFor,
							trackingId));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException e) {
			LOGGER.error("Error trying to check computation status.", e);
			return Response.status(e.getHttpCode()).build();
		}
	}

}
