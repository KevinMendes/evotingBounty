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
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.voteverification.service.ChoiceReturnCodesService;

/**
 * Web service for retrieving the short Choice Return Codes (in collaboration with the control components).
 */
@Stateless
@Path(ChoiceReturnCodesResource.RESOURCE_PATH)
public class ChoiceReturnCodesResource {

	static final String RESOURCE_PATH = "/choicecodes";

	private static final String GENERATE_CHOICE_CODES_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}";
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	private static final String PARAMETER_VALUE_VERIFICATION_CARD_ID = "verificationCardId";
	private static final String RESOURCE = "CHOICE_CODES";
	private static final String ERROR_CODE_MANDATORY_FIELD = "mandatory.field";
	private static final String VERIFIATION_CARD_ID_IS_NULL = "Verification card id is null";
	private static final String ELECTION_EVENT_ID_IS_NULL = "Election event id is null";
	private static final String TENANT_ID_IS_NULL = "Tenant id is null";

	private final TrackIdInstance trackIdInstance;
	private final ChoiceReturnCodesService choiceReturnCodesService;

	@Inject
	public ChoiceReturnCodesResource(final TrackIdInstance trackIdInstance, final ChoiceReturnCodesService choiceReturnCodesService) {
		this.trackIdInstance = trackIdInstance;
		this.choiceReturnCodesService = choiceReturnCodesService;
	}

	/**
	 * Retrieves the choice codes taking into account the election event and verification card for a given encrypted vote.
	 */
	@Path(GENERATE_CHOICE_CODES_PATH)
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response retrieveShortChoiceReturnCodes(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_VERIFICATION_CARD_ID)
			final String verificationCardId,
			@NotNull
			final Reader voteReader,
			@Context
			final HttpServletRequest request)
			throws IOException, SyntaxErrorException, SemanticErrorException, ResourceNotFoundException,
			CryptographicOperationException {

		// Set the track id to be logged.
		trackIdInstance.setTrackId(trackingId);

		checkNotNull(tenantId);
		checkNotNull(electionEventId);
		checkNotNull(verificationCardId);

		// Convert json to object.
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		final VoteAndComputeResults voteAndComputeResults = objectMapper.readValue(voteReader, VoteAndComputeResults.class);

		// Validate voter verification.
		ValidationUtils.validate(voteAndComputeResults.getVote());

		final ShortChoiceReturnCodeAndComputeResults shortChoiceReturnCodesAndComputeResults = choiceReturnCodesService.retrieveShortChoiceReturnCodes(
				tenantId, electionEventId, verificationCardId, voteAndComputeResults);

		return Response.ok(shortChoiceReturnCodesAndComputeResults).build();
	}

}
