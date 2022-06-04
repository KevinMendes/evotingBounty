/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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

import com.google.gson.Gson;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.ElectionEventData;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.ElectionEventService;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.ElectionEventException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * Web service for handling election event data.
 */
@Path("/electioneventdata")
@Stateless(name = "au-ElectionEventDataResource")
public class ElectionEventDataResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventDataResource.class);

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";

	private final TrackIdInstance trackIdInstance;
	private final ElectionEventService electionEventService;

	@Inject
	public ElectionEventDataResource(final ElectionEventService electionEventService, final TrackIdInstance trackIdInstance) {
		this.electionEventService = electionEventService;
		this.trackIdInstance = trackIdInstance;
	}

	/**
	 * Save the election event data given the tenant and the election event id.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param request         - the http servlet request.
	 * @return status 200 on success.
	 * @throws ApplicationException if the input parameters are not valid.
	 */
	@POST
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/adminboard/{adminBoardId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveElectionEventData(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final ElectionEventData electionEventData,
			@Context
			final HttpServletRequest request) throws ApplicationException, IOException {

		trackIdInstance.setTrackId(trackingId);

		try {
			electionEventService.saveElectionEventData(tenantId, electionEventId, adminBoardId, electionEventData);
			return Response.ok().build();
		} catch (final ElectionEventException e) {
			LOGGER.error("Error while saving election information", e);
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
	}

	/**
	 * Check if the election event data is empty.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/status")
	public Response checkIfElectionEventDataIsEmpty(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@Context
			final HttpServletRequest request) {

		trackIdInstance.setTrackId(trackingId);

		final ValidationResult validationResult = electionEventService.checkIfElectionEventDataIsEmpty(tenantId, electionEventId);

		final Gson gson = new Gson();
		// convert to string
		final String json = gson.toJson(validationResult);

		return Response.ok().entity(json).build();
	}
}
