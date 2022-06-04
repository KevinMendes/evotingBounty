/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.operation;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
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

import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.BallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecBallotBoxService;

@Path(MixDecBallotBoxesResource.RESOURCE_PATH)
@Stateless(name = "or-MixDecBallotBoxesResource")
public class MixDecBallotBoxesResource {

	/* Base path to resource */
	static final String RESOURCE_PATH = "mixdec/secured/tenant/{tenantId}/electionevent/{electionEventId}/ballotboxes";
	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecBallotBoxesResource.class);
	private static final String PATH_PARAMETER_TENANT_ID = "tenantId";

	private static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	@Inject
	private MixDecBallotBoxService mixDecBallotBoxService;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Validates parameters common to all requests.
	 */
	private static void validateParameters(String tenantId, String electionEventId) throws ApplicationException {
		if ((null == tenantId) || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_TENANT_ID);
		}

		if ((null == electionEventId) || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_ELECTION_EVENT_ID);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response processBallotBoxes(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
					List<String> ballotBoxIds,
			@Context
					HttpServletRequest request) throws ApplicationException {

		validateParameters(tenantId, electionEventId);
		ballotBoxIds.forEach(UUIDValidations::validateUUID);

		trackIdInstance.setTrackId(trackingId);

		LOGGER.info("Received ballot boxes for mixing: {}.", ballotBoxIds);
		List<BallotBoxStatus> ballotBoxStatusList = mixDecBallotBoxService.processBallotBoxes(electionEventId, ballotBoxIds, trackingId);

		return Response.ok().entity(ballotBoxStatusList).build();
	}

}
