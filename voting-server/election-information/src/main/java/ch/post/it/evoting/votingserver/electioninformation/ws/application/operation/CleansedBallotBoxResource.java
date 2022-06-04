/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.operation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxIdImpl;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxServiceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox.BallotBoxService;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox.CleansedBallotBoxService;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

@Path(CleansedBallotBoxResource.RESOURCE_NAME)
@Stateless
public class CleansedBallotBoxResource {

	static final String RESOURCE_NAME = "cleansedballotboxes";

	private static final String CLEANSED_BALLOT_BOX_PATH = "tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}";
	private static final String ENCRYPTED_VOTES_PATH = CLEANSED_BALLOT_BOX_PATH + "/encryptedvotes";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String QUERY_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";
	private static final Logger LOGGER = LoggerFactory.getLogger(CleansedBallotBoxResource.class);

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private CleansedBallotBoxService cleansedBallotBoxService;

	@Inject
	private BallotBoxService ballotBoxService;

	@Inject
	private ElectionService electionService;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(CLEANSED_BALLOT_BOX_PATH)
	public Response isBallotBoxEmpty(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) {

		trackIdInstance.setTrackId(trackingId);

		final boolean ballotBoxEmpty = cleansedBallotBoxService.isBallotBoxEmpty(electionEventId, ballotBoxId);

		// Create response json.
		final ObjectMapper mapper = new ObjectMapper();
		final ObjectNode responseJson = mapper.createObjectNode();
		responseJson.put(QUERY_PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		responseJson.put("empty", ballotBoxEmpty);

		return Response.ok().entity(responseJson).build();
	}

	/**
	 * Gets the initial payload from a ballot box as a json, to be send to the first mixing control component.
	 *
	 * @param trackingId      the tracking id.
	 * @param tenantId        the tenant id. Must be non-null and non-empty.
	 * @param electionEventId the election event id corresponding to the ballot box. Must be non-null and non-empty.
	 * @param ballotBoxId     the ballot box id. Must be non-null and non-empty.
	 * @param request         the request context.
	 * @return the payload for the given ballot box.
	 * @throws ApplicationException if any of the tenantId, electionEventId or ballotBoxId parameters is null or empty.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(ENCRYPTED_VOTES_PATH)
	public Response getMixnetInitialPayload(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		trackIdInstance.setTrackId(trackingId);

		// validate parameters
		validateParameters(tenantId, electionEventId, ballotBoxId);

		try {

			if (!ballotBoxService.checkIfTest(tenantId, electionEventId, ballotBoxId)) {
				// The grace period will be included in the validation of the election dates
				final ElectionValidationRequest electionValidationRequest = ElectionValidationRequest
						.create(tenantId, electionEventId, ballotBoxId, true);
				final ValidationError validationResult = electionService.validateIfElectionIsOpen(electionValidationRequest);

				if (!ValidationErrorType.ELECTION_OVER_DATE.equals(validationResult.getValidationErrorType())) {
					LOGGER.info("Ballot box {} not closed therefore not downloadable.", ballotBoxId);
					return Response.status(Response.Status.PRECONDITION_FAILED).build();
				}
			}

		} catch (final ResourceNotFoundException e) {
			LOGGER.error(String.format("Trying to download unknown ballot box: %s", ballotBoxId), e);
			return Response.status(Response.Status.NOT_FOUND).build();
		}

		final BallotBoxId bbid = new BallotBoxIdImpl(tenantId, electionEventId, ballotBoxId);

		try {
			final MixnetInitialPayload mixnetInitialPayload = cleansedBallotBoxService.getMixnetInitialPayload(bbid);

			return Response.ok().entity(mixnetInitialPayload).build();
		} catch (final ResourceNotFoundException | CleansedBallotBoxServiceException e) {
			LOGGER.error("Failed to retrieve encrypted votes.", e);
			return Response.serverError().entity(e).build();
		}
	}

	// Validate parameters
	private void validateParameters(final String tenantId, final String electionEventId, final String ballotBoxId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}
		if (ballotBoxId == null || ballotBoxId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_BALLOT_BOX_ID);
		}

	}
}
