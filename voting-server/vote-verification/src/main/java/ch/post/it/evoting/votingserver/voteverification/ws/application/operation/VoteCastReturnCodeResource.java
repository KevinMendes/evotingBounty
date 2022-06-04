/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.confirmation.TraceableConfirmationMessage;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.voteverification.service.VoteCastReturnCodeService;

/**
 * Web service for retrieving the short Vote Cast Return Code (in collaboration with the control components).
 */
@Stateless
@Path(VoteCastReturnCodeResource.RESOURCE_PATH)
public class VoteCastReturnCodeResource {

	static final String RESOURCE_PATH = "/castcodes";

	private static final String RETRIEVE_CAST_CODES_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}";
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	private static final String PARAMETER_VALUE_VERIFICATION_CARD_ID = "verificationCardId";

	private final TrackIdInstance tackIdInstance;
	private final VoteCastReturnCodeService castCodeService;

	@Inject
	public VoteCastReturnCodeResource(final TrackIdInstance trackIdInstance, final VoteCastReturnCodeService voteCastReturnCodeService) {
		this.tackIdInstance = trackIdInstance;
		this.castCodeService = voteCastReturnCodeService;
	}

	/**
	 * Retrieves the short Vote Cast Return Code for a given election event ID and verification card ID using a Confirmation Key.
	 *
	 * @param trackingId                the track id to be used for logging purposes.
	 * @param tenantId                  the tenant identifier.
	 * @param electionEventId           the election event identifier.
	 * @param verificationCardId        the verification card identifier.
	 * @param confirmationMessageReader the confirmation message to process.
	 * @param request                   the http servlet request.
	 * @return the http response of executing the operation. HTTP status code 200 if the request has succeeded.
	 * @throws SemanticErrorException if there are semantic errors in the data included in the body of the request.
	 * @throws SyntaxErrorException   if the URI is incorrect or if the body of the request has syntax errors (For instance, a missing field).
	 */
	@Path(RETRIEVE_CAST_CODES_PATH)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveShortVoteCastCode(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_VERIFICATION_CARD_ID)
			final String verificationCardId,
			@NotNull
			final Reader confirmationMessageReader,
			@Context
			final HttpServletRequest request)
			throws SyntaxErrorException, SemanticErrorException, IOException, ResourceNotFoundException,
			CryptographicOperationException {

		// Set the track id to be logged.
		tackIdInstance.setTrackId(trackingId);

		checkNotNull(tenantId);
		checkNotNull(electionEventId);
		checkNotNull(verificationCardId);

		// Convert json to object.
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final TraceableConfirmationMessage traceableConfirmationMessage = objectMapper.readValue(confirmationMessageReader,
				TraceableConfirmationMessage.class);

		// Validate voter verification.
		ValidationUtils.validate(traceableConfirmationMessage);

		final ShortVoteCastReturnCodeAndComputeResults shortVoteCastReturnCodeMessage = castCodeService.retrieveShortVoteCastCode(tenantId,
				electionEventId, verificationCardId, traceableConfirmationMessage);

		return Response.ok().entity(shortVoteCastReturnCodeMessage).build();

	}

}
