/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.operation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ChoiceCodesComputationStatus;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services.ChoiceCodesGenerationContributionsService;

@Path(ChoiceCodesResource.RESOURCE_PATH)
@Stateless(name = "or-ChoiceCodesResource")
public class ChoiceCodesResource {

	/* Base path to resource */
	static final String RESOURCE_PATH = "choicecodes";

	static final String PATH_COMPUTE_GENERATION_CONTRIBUTIONS_REQUEST = "computeGenerationContributions";

	static final String PATH_COMPUTE_GENERATION_CONTRIBUTIONS_RETRIEVAL = "tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/chunkId/{chunkId}/computeGenerationContributions";

	static final String PATH_COMPUTE_GENERATION_CONTRIBUTIONS_STATUS = "tenant/{tenantId}/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/generationContributions/status";

	static final String PATH_PARAMETER_TENANT_ID = "tenantId";

	static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	static final String PATH_PARAMETER_VERIFICATION_CARD_ID = "verificationCardId";

	static final String PATH_PARAMETER_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	static final String PATH_PARAMETER_CHUNK_ID = "chunkId";

	static final String QUERY_PARAMETER_CHUNK_COUNT = "chunkCount";

	@Inject
	private Logger logger;

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private ChoiceCodesGenerationContributionsService choiceCodesGenerationContributionsService;

	@POST
	@Path(PATH_COMPUTE_GENERATION_CONTRIBUTIONS_REQUEST)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChoiceCodeNodesComputeForGenerationContributions(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@NotNull
			final ReturnCodeGenerationRequestPayload payload,
			@Context
			final HttpServletRequest request) throws ApplicationException, DuplicateEntryException, ResourceNotFoundException {

		trackIdInstance.setTrackId(trackingId);

		final String tenantId = payload.getTenantId();
		final String electionEventId = payload.getElectionEventId();
		final String verificationCardSetId = payload.getVerificationCardSetId();
		final int chunkId = payload.getChunkId();

		validateParameters(tenantId, electionEventId, verificationCardSetId);

		logger.info("OR:{} - Requesting collection of the choice codes generation phase compute contributions for tenant {}"
						+ " electionEventId {} verificationCardSetId {} and chunkId {}", trackingId, tenantId, electionEventId, verificationCardSetId,
				chunkId);

		choiceCodesGenerationContributionsService.request(trackingId, payload);

		return Response.ok().build();
	}

	@GET
	@Path(PATH_COMPUTE_GENERATION_CONTRIBUTIONS_RETRIEVAL)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response getChoiceCodeNodesComputeForGenerationContributions(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@PathParam(PATH_PARAMETER_CHUNK_ID)
			final int chunkId,
			@Context
			final HttpServletRequest request) throws ApplicationException, ResourceNotFoundException {

		trackIdInstance.setTrackId(trackingId);

		validateParameters(tenantId, electionEventId, verificationCardSetId);

		logger.info("Retrieving collection of the choice codes generation phase compute contributions for tenant {} electionEventId {} "
				+ "and verificationCardSetId {}", tenantId, electionEventId, verificationCardSetId);

		final ChoiceCodesComputationStatus contributionsStatus = choiceCodesGenerationContributionsService
				.getComputedValuesStatus(electionEventId, tenantId, verificationCardSetId, chunkId);

		if (ChoiceCodesComputationStatus.COMPUTED.equals(contributionsStatus)) {

			final StreamingOutput entity = stream -> {
				try {
					choiceCodesGenerationContributionsService.writeToStream(stream, tenantId, electionEventId, verificationCardSetId, chunkId);
				} catch (ResourceNotFoundException e) {
					throw new WebApplicationException(e, Status.NOT_FOUND);
				}
			};

			return Response.ok().entity(entity).header("Content-Disposition", "attachment;").build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path(PATH_COMPUTE_GENERATION_CONTRIBUTIONS_STATUS)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getChoiceCodesGenerationComputationStatus(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@QueryParam(QUERY_PARAMETER_CHUNK_COUNT)
			final int chunkCount,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		trackIdInstance.setTrackId(trackingId);

		validateParameters(tenantId, electionEventId, verificationCardSetId);

		logger.info("Checking status of the choice codes generation phase compute contributions for tenant {} electionEventId {} "
				+ "and verificationCardSetId {}", tenantId, electionEventId, verificationCardSetId);

		try {
			final ChoiceCodesComputationStatus contributionsStatus = choiceCodesGenerationContributionsService
					.getCompositeComputedValuesStatus(electionEventId, tenantId, verificationCardSetId, chunkCount);
			final String result = Json.createObjectBuilder().add("status", contributionsStatus.name()).build().toString();

			return Response.ok().entity(result).build();

		} catch (ResourceNotFoundException e) {
			logger.error("Resource not found matching the received parameters", e);
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private void validateParameters(final String tenantId, final String electionEventId, final String verificationCardSetId)
			throws ApplicationException {

		validateTenantIDAndElectionEventID(tenantId, electionEventId);

		if (verificationCardSetId == null || verificationCardSetId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_VERIFICATION_CARD_ID);
		}
	}

	private static void validateTenantIDAndElectionEventID(final String tenantId, final String electionEventId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_ELECTION_EVENT_ID);
		}
	}
}
