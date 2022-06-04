/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.operation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox.BallotBoxService;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

/**
 * Web service for handling ballot box information.
 */
@Path("/ballotboxes")
@Stateless
public class BallotBoxResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxResource.class);

	private static final String RESOURCE_NAME = "ballotboxes";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String QUERY_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	private final Gson gson = new Gson();

	// The track id instance
	@Inject
	private TrackIdInstance trackIdInstance;

	@EJB
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	@Inject
	private BallotBoxService ballotBoxService;

	@Inject
	private ElectionService electionService;

	/**
	 * Return a ballot box information given the tenant, the election event and the ballot identifiers.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot identifier.
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return Returns the corresponding ballot information for the tenantId, electionEventId and ballotId.
	 * @throws ApplicationException      if the input parameters are not valid.
	 * @throws ResourceNotFoundException if the ballot is not found.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}")
	public Response getBallotBoxInformation(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) throws ApplicationException, ResourceNotFoundException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		// validate parameters
		validateParameters(tenantId, electionEventId, ballotBoxId);

		// search ballot information
		LOGGER.info("Getting ballot box information... [ballotBoxId: {}, electionEventId: {}, tenantId: {}]", ballotBoxId, electionEventId,
				tenantId);

		final BallotBoxInformation ballotBoxInformation = ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId,
				electionEventId, ballotBoxId);

		LOGGER.info("Ballot box information found. [ballotBoxId: {}, electionEventId: {}, tenantId: {}]", ballotBoxId, electionEventId,
				tenantId);

		final JsonObject bbInfoJsonObject = JsonUtils.getJsonObject(ballotBoxInformation.getJson());

		// send json value of ballot information
		return Response.ok().entity(gson.toJson(bbInfoJsonObject.toString())).build();
	}

	/**
	 * Return an encrypted ballot box given the tenant, the election event and the ballot box identifiers.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot identifier.
	 * @return Returns the corresponding ballot box for the tenantId, electionEventId and ballotBoxId.
	 * @throws ResourceNotFoundException if the ballot box is not found.
	 * @throws ApplicationException      if the input parameters are not valid.
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM })
	@Path("secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}")
	public Response getEncryptedBallotBoxes(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) throws ResourceNotFoundException, ApplicationException {

		trackIdInstance.setTrackId(trackingId);

		// validate parameters
		validateParameters(tenantId, electionEventId, ballotBoxId);

		final boolean test = ballotBoxService.checkIfTest(tenantId, electionEventId, ballotBoxId);
		if (!test) {
			// The grace period will be included in the validation of the
			// election dates
			final ElectionValidationRequest electionValidationRequest = ElectionValidationRequest.create(tenantId, electionEventId, ballotBoxId,
					true);
			final ValidationError validationResult = electionService.validateIfElectionIsOpen(electionValidationRequest);
			if (!ValidationErrorType.ELECTION_OVER_DATE.equals(validationResult.getValidationErrorType())) {
				return Response.status(Status.PRECONDITION_FAILED).build();
			}
		}

		final StreamingOutput entity = stream -> ballotBoxService.writeEncryptedBallotBox(stream, tenantId, electionEventId, ballotBoxId, test);
		return Response.ok().entity(entity).header("Content-Disposition", "attachment; filename=EncryptedBallotBox.csv;").build();
	}

	/**
	 * Returns the result of validate if all ballot boxes for a given tenant, election event and ballot box are empty.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot box identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/status")
	public Response checkIfBallotBoxIsEmpty(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) {

		trackIdInstance.setTrackId(trackingId);

		final ValidationResult validationResult = ballotBoxService.checkIfBallotBoxesAreEmpty(tenantId, electionEventId, ballotBoxId);

		// convert to string
		final String json = gson.toJson(validationResult);

		return Response.ok().entity(json).build();
	}

	/**
	 * Returns the result of validate if all ballot boxes for a given tenant, election event and ballot box is available.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotBoxId     - the ballot box identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/available")
	public Response checkIfBallotBoxIsAvailable(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) {

		trackIdInstance.setTrackId(trackingId);

		// The grace period wont'be considered for this validation
		final ElectionValidationRequest electionValidationRequest = ElectionValidationRequest.create(tenantId, electionEventId, ballotBoxId, false);
		final ValidationError validationResult = electionService.validateIfElectionIsOpen(electionValidationRequest);

		// convert to string
		final String json = gson.toJson(validationResult);

		return Response.ok().entity(json).build();
	}

	// Validate parameters.
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
