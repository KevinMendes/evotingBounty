/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.operation;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetPayload;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxIdImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.BallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecBallotBoxService;

@Path(MixDecBallotBoxResource.RESOURCE_PATH)
@Stateless(name = "or-MixDecBallotBoxResource")
public class MixDecBallotBoxResource {

	/* Base path to resource */
	static final String RESOURCE_PATH = "/mixdec/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}";

	static final String PATH_STATUS = "status";

	static final String PATH_MIXNET_INITIAL_PAYLOADS = "mixnetInitialPayloads";

	static final String PATH_MIXNET_SHUFFLE_PAYLOADS = "mixnetShufflePayloads";

	private static final String PATH_PARAMETER_TENANT_ID = "tenantId";

	private static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String PATH_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecBallotBoxResource.class);
	@Inject
	private MixDecBallotBoxService mixDecBallotBoxService;
	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Validates mixing and decryption parameters.
	 */
	private static void validateParameters(String tenantId, String electionEventId, String ballotBoxId) throws ApplicationException {
		if ((null == tenantId) || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_TENANT_ID);
		}

		if ((null == electionEventId) || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_ELECTION_EVENT_ID);
		}

		if ((null == ballotBoxId) || ballotBoxId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_BALLOT_BOX_ID);
		}
	}

	@GET
	@Path(PATH_STATUS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBallotBoxMixingStatus(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		trackIdInstance.setTrackId(trackingId);

		validateParameters(tenantId, electionEventId, ballotBoxId);

		LOGGER.info("Checking mixing status of the ballot box for tenant {} electionEventId {} and ballotBoxId {}", tenantId, electionEventId,
				ballotBoxId);

		List<BallotBoxStatus> mixDecBallotBoxStatus = mixDecBallotBoxService.getMixDecBallotBoxStatus(electionEventId, new String[] { ballotBoxId });

		String result = Json.createObjectBuilder().add(PATH_STATUS, mixDecBallotBoxStatus.get(0).getProcessStatus().toString()).build().toString();

		return Response.ok().entity(result).build();
	}

	/**
	 * Gets the payloads for the specified ballot box.
	 *
	 * @param trackingId      an identifier for the current request
	 * @param tenantId        the tenant that owns the ballot box
	 * @param electionEventId the election event the ballot box belongs to
	 * @param ballotBoxId     the identifier of the ballot box
	 * @param request
	 * @return a JSON with the payloads
	 * @throws ApplicationException
	 */
	@GET
	@Path(PATH_MIXNET_INITIAL_PAYLOADS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMixnetInitialPayloads(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
					String ballotBoxId,
			@Context
					HttpServletRequest request) throws ApplicationException {

		return getMixnetPayloads(trackingId, tenantId, electionEventId, ballotBoxId, true);
	}

	/**
	 * Gets the payloads for the specified ballot box.
	 *
	 * @param trackingId      an identifier for the current request
	 * @param tenantId        the tenant that owns the ballot box
	 * @param electionEventId the election event the ballot box belongs to
	 * @param ballotBoxId     the identifier of the ballot box
	 * @param request
	 * @return a JSON with the payloads
	 * @throws ApplicationException
	 */
	@GET
	@Path(PATH_MIXNET_SHUFFLE_PAYLOADS)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMixnetShufflePayloads(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PATH_PARAMETER_BALLOT_BOX_ID)
					String ballotBoxId,
			@Context
					HttpServletRequest request) throws ApplicationException {

		return getMixnetPayloads(trackingId, tenantId, electionEventId, ballotBoxId, false);
	}

	private Response getMixnetPayloads(final String trackingId,
			final String tenantId,
			final String electionEventId,
			final String ballotBoxId,
			final boolean getInitial) throws ApplicationException {
		trackIdInstance.setTrackId(trackingId);

		validateParameters(tenantId, electionEventId, ballotBoxId);

		BallotBoxId bbid = new BallotBoxIdImpl(tenantId, electionEventId, ballotBoxId);
		LOGGER.info("Getting node outputs for ballot box {}...", bbid);

		List<BallotBoxStatus> ballotBoxStatuses = mixDecBallotBoxService.getMixDecBallotBoxStatus(electionEventId, new String[] { ballotBoxId });

		Response response;
		if (ballotBoxStatuses.isEmpty()) {
			LOGGER.warn("The status of ballot box {} is not available", bbid);
			response = Response.status(Status.NOT_FOUND).build();
		} else {
			if (MixDecStatus.MIXED.equals(ballotBoxStatuses.get(0).getProcessStatus())) {
				LOGGER.info("Preparing the payload JSONs for ballot box {}...", bbid);
				final List<MixnetPayload> ballotBoxPayloadList = mixDecBallotBoxService.getBallotBoxPayloads(bbid, getInitial);
				response = Response.ok(ballotBoxPayloadList).build();
			} else {
				String errorMessage = ballotBoxStatuses.get(0).getErrorMessage();
				LOGGER.warn("Ballot box {} is not mixed. {}", bbid, errorMessage);
				response = Response.status(Status.NOT_FOUND).entity(errorMessage).build();
			}
		}

		return response;
	}
}
