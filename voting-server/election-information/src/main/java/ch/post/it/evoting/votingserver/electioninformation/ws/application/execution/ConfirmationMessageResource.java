/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.execution;

import java.io.IOException;

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

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.confirmation.ConfirmationMessageValidationService;

/**
 * The endpoint for confirmation messages
 */
@Path(ConfirmationMessageResource.RESOURCE_PATH)
@Stateless
public class ConfirmationMessageResource {

	static final String RESOURCE_PATH = "/confirmations";

	static final String VALIDATE_CONFIRMATION_MESSAGE_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}";

	private static final String AUTHENTICATION_TOKEN = "authenticationToken";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfirmationMessageResource.class);
	@Inject
	private ConfirmationMessageValidationService confirmationMessageValidationService;
	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Receives a confirmation message with additional information and validates this information.
	 *
	 * @param tenantId                      - Tenant identifier
	 * @param electionEventId               - election event identifier
	 * @param votingCardId                  - voting card identifier
	 * @param confirmationInformation       - confirmation information to be validated
	 * @param authenticationTokenJsonString - the authentication token
	 * @param trackingId                    - the request id for logging purposes
	 * @param request                       - the http servlet request.
	 * @return the result of the validation
	 */
	@POST
	@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(ConfirmationMessageResource.VALIDATE_CONFIRMATION_MESSAGE_PATH)
	public Response validateConfirmationMessage(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final
			String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final
			String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final
			String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
			final
			String votingCardId, final ConfirmationInformation confirmationInformation,
			@NotNull
			@HeaderParam(AUTHENTICATION_TOKEN)
			final
			String authenticationTokenJsonString,
			@Context
			final
			HttpServletRequest request) {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		final AuthenticationToken authenticationToken;
		try {
			// convert from json to object
			authenticationToken = DomainObjectMapper.getNewInstance().readValue(authenticationTokenJsonString, AuthenticationToken.class);

			// validate auth token
			ValidationUtils.validate(authenticationToken);
		} catch (final IOException | IllegalArgumentException | ValidationException e) {
			LOGGER.error("Invalid authentication token format.", e);
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}

		final ConfirmationInformationResult result = confirmationMessageValidationService
				.validateConfirmationMessage(tenantId, electionEventId, votingCardId, confirmationInformation, authenticationToken);
		return Response.ok().entity(result).build();
	}
}
