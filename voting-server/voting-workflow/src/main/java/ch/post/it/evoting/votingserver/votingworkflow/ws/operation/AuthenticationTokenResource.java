/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObject;
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

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenService;

/**
 * The end point for the Authentication Tokens.
 */
@Path("/tokens")
@Stateless(name = "vw-AuthenticationTokenResource")
public class AuthenticationTokenResource {

	// The name of the parameter value tenant id.
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	// The name of the parameter value credential id.
	private static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	// The name of the parameter value election event id.
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	// Tracking id used for logging and tracking purposes
	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private AuthenticationTokenService authenticationTokenService;

	/**
	 * Gets the authentication token which matches with the given parameters.
	 *
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param credentialId         - the credential Identifier.
	 * @param challengeInformation - the challenge information including client challenge, server
	 *                             challenge, and server timestamp.
	 * @param request              - the http servlet request.
	 * @return if the operation is successfully performed, the associated authentication token
	 * @throws ApplicationException      if parameter validation fails
	 * @throws ResourceNotFoundException if the voter information is not found
	 * @throws GeneralCryptoLibException if length is out of the range for this generator during
	 *                                   creation of token or in requestId generation.
	 * @throws SemanticErrorException    if there are semantic problems during validation of input
	 *                                   parameters.
	 * @throws SyntaxErrorException      if there are syntax problems during validation of input
	 *                                   parameters.
	 * @throws DuplicateEntryException   if the unique constraint is violated.
	 * @throws EntryPersistenceException if fails trying to update an entity.
	 */
	@POST
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAuthenticationInformation(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@NotNull
					ChallengeInformation challengeInformation,
			@Context
					HttpServletRequest request)
			throws ApplicationException, ResourceNotFoundException, GeneralCryptoLibException, IOException, SyntaxErrorException,
			SemanticErrorException, DuplicateEntryException, EntryPersistenceException {

		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, credentialId);

		// validate input challengeInformation for syntax or semantic errors
		ValidationUtils.validate(challengeInformation);

		// generates the authentication token message with auth token and
		// validation error
		JsonObject authTokenMessage = authenticationTokenService
				.getAuthenticationToken(tenantId, electionEventId, credentialId, challengeInformation);

		// Returns authentication token
		return Response.ok().entity(authTokenMessage.toString()).build();
	}

	// Does a basic validation of the input. In case something is wrong, just
	// throws an exception.
	private void validateInput(String tenantId, String electionEventId, String credentialId) throws ApplicationException {
		if (tenantId == null || "".equals(tenantId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || "".equals(electionEventId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (credentialId == null || "".equals(credentialId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_CREDENTIAL_ID_IS_NULL);
		}
	}
}
