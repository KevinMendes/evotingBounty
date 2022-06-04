/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.operation;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
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

import com.google.gson.Gson;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotText;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotTextRepository;

/**
 * Web service for handling text for internationalization of ballot.
 */
@Path("/ballottexts")
@Stateless
public class BallotTextResource {

	private static final String RESOURCE_NAME = "ballottexts";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_BALLOT_ID = "ballotId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String JSON_PARAMETER_BALLOT_TEXTS = "ballotTexts";

	private static final String JSON_PARAMETER_BALLOT_TEXTS_SIGNATURE = "ballotTextsSignature";
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotTextResource.class);
	private final Gson gson = new Gson();
	@Inject
	private TrackIdInstance trackIdInstance;
	@EJB
	private BallotTextRepository ballotTextRepository;

	/**
	 * Return a ballot text given the tenant, election event and the ballot identifiers.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotId        - the ballot identifier.
	 * @param trackId         - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return Returns the corresponding ballot text for the tenantId, electionEventId and ballotId.
	 * @throws ApplicationException      if the input parameters are not valid.
	 * @throws IOException               if there are errors during conversion of ballot to json format.
	 * @throws ResourceNotFoundException if the ballot is not found.
	 */
	@GET
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getBallot(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_ID)
					String ballotId,
			@Context
					HttpServletRequest request) throws ApplicationException, IOException, ResourceNotFoundException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		LOGGER.info("Getting ballot text for ballotId: {}, electionEventId: {} and tenantId: {}.", ballotId, electionEventId, tenantId);

		// validate parameters
		validateParameters(tenantId, electionEventId, ballotId);

		// search ballot text
		BallotText ballotText = ballotTextRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);

		LOGGER.info("Ballot text with ballotId: {}, electionEventId: {} and tenantId: {} found.", ballotId, electionEventId, tenantId);

		// convert to string
		JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add(JSON_PARAMETER_BALLOT_TEXTS, JsonUtils.getJsonArray(ballotText.getJson()));
		jsonObjectBuilder.add(JSON_PARAMETER_BALLOT_TEXTS_SIGNATURE, JsonUtils.getJsonArray(ballotText.getSignature()));
		String json = gson.toJson(jsonObjectBuilder.build().toString());

		// return the ballot text json
		return Response.ok().entity(json).build();
	}

	// Validate parameters.
	private void validateParameters(String tenantId, String electionEventId, String ballotId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}

		if (ballotId == null || ballotId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_BALLOT_ID);
		}
	}
}
