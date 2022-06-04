/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;
import java.util.List;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.service.ElectionEventContextService;

/**
 * Web service for saving and retrieving the election event context.
 */
@Path(ElectionEventContextResource.RESOURCE_PATH)
@Stateless
public class ElectionEventContextResource {

	static final String RESOURCE_PATH = "/configuration/electioncontext";

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextResource.class);

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	private static final String RESOURCE_NAME = "configuration/electioncontext";

	@Inject
	private ElectionEventContextService electionEventContextService;

	@Inject
	private TrackIdInstance tackIdInstance;

	@Inject
	private ObjectMapper objectMapper;

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveElectionEventContext(
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
					ElectionEventContextPayload electionEventContextPayload,
			@Context
					HttpServletRequest request) {

		tackIdInstance.setTrackId(trackingId);

		final String electionEventId = electionEventContextPayload.getElectionEventContext().getElectionEventId();

		final List<ElectionContextResponsePayload> electionContextResponsePayloads;
		try {
			electionContextResponsePayloads = electionEventContextService.saveElectionEventContext(electionEventContextPayload);
		} catch (DuplicateEntryException e) {
			LOGGER.warn("Duplicate entry tried to be inserted. [electionEventId: {}]", electionEventId);
			return Response.noContent().build();
		} catch (RetrofitException e) {
			LOGGER.warn("Error trying to save election event context. [electionEventId: {}]", electionEventId);
			return Response.status(e.getHttpCode()).build();
		}

		LOGGER.info("Successfully saved the election event context. [electionEventId: {}]", electionEventId);

		return Response.ok(electionContextResponsePayloads).build();
	}

	@Path("/electionevent/{electionEventId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveElectionEventContext(
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Context
					HttpServletRequest request) throws IOException, ApplicationException {

		tackIdInstance.setTrackId(trackingId);
		validateInput(electionEventId);

		final ElectionEventContextEntity electionEventContextEntity;
		try {
			electionEventContextEntity = electionEventContextService.retrieveElectionEventContext(electionEventId);
		} catch (ResourceNotFoundException e) {
			LOGGER.warn("No election event context retrieved. [electionEventId: {}]", electionEventId);
			return Response.noContent().build();
		}

		LOGGER.info("Successfully retrieved the election event context. [electionEventId: {}]", electionEventId);

		String jsonElectionEventContext = objectMapper.writeValueAsString(electionEventContextEntity);
		return Response.ok().entity(jsonElectionEventContext).build();
	}

	private void validateInput(String electionEventId) throws ApplicationException {
		try {
			validateUUID(electionEventId);
		} catch (FailedValidationException | NullPointerException e) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MANDATORY_FIELD, PARAMETER_VALUE_ELECTION_EVENT_ID);
		}
	}
}
