/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

import java.io.IOException;
import java.security.cert.CertificateException;

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

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.validation.AuthenticationTokenValidationService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;

/**
 * The end point for validating authentication token resources.
 */
@Path("/validations")
@Stateless
public class ValidationAuthenticationTokenResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_AUTHENTICATION_TOKEN = "authenticationToken";

	private static final String WHITESPACE_REGEXP = "\\s+";
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationAuthenticationTokenResource.class);
	@Inject
	private TrackIdInstance trackIdInstance;
	@Inject
	private AuthenticationTokenValidationService authenticationTokenValidationService;

	/**
	 * Return a AuthenticationTokenValidationResult object with the result of the authentication
	 * token validation.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @param tokenString     - the authentication token in JSON format received in the header.
	 * @param request         - the HTTP servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code
	 * 200 and the AuthenticationTokenValidationResult in JSON format.
	 * @throws ApplicationException      if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws IOException               if the authenticationToken is invalid and can not be mapped correctly to an
	 *                                   object.
	 * @throws SemanticErrorException    if the authenticationToken is not valid due to semantic errors.
	 * @throws SyntaxErrorException      if the authenticationToken is not valid due to syntax errors.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateAuthenticationToken(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@HeaderParam(PARAMETER_AUTHENTICATION_TOKEN)
			@NotNull
					String tokenString,
			@Context
					HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, IOException, CertificateException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, votingCardId, tokenString);

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		AuthenticationToken authenticationToken = objectMapper.readValue(tokenString, AuthenticationToken.class);

		Response response;
		ValidationResult validationResult;
		try {
			// validate input authenticationToken for syntax or semantic errors
			ValidationUtils.validate(authenticationToken);
			// validate the authentication token
			validationResult = authenticationTokenValidationService.validate(tenantId, electionEventId, votingCardId, authenticationToken);

			// Currently we return 200 OK even if the validation fails
			if (validationResult.isResult()) {
				response = Response.ok(validationResult).build();
			} else {
				response = Response.status(PRECONDITION_FAILED).entity(validationResult).build();
			}
		} catch (ValidationException e) {
			final String error = "Invalid authentication token format.";
			LOGGER.error(error, e);

			validationResult = new ValidationResult(false);
			ValidationError validationError = new ValidationError(ValidationErrorType.FAILED);
			validationError.setErrorArgs(new String[] { error });
			validationResult.setValidationError(validationError);
			response = Response.status(PRECONDITION_FAILED).entity(validationResult).build();
		}

		return response;
	}

	private void validateInput(String tenantId, String electionEventId, String votingCardId, String tokenString)
			throws ApplicationException, IOException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (votingCardId == null || votingCardId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_VOTING_CARD_ID_IS_NULL);
		}
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		AuthenticationToken authenticationToken = objectMapper.readValue(tokenString, AuthenticationToken.class);
		String serialized = objectMapper.writeValueAsString(authenticationToken);
		String strippedOriginalToken = tokenString.replaceAll(WHITESPACE_REGEXP, "");
		String strippedSerializedToken = serialized.replaceAll(WHITESPACE_REGEXP, "");

		if (strippedOriginalToken.length() != strippedSerializedToken.length()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_AUTH_TOKEN_INVALID_FORMAT);
		}
	}
}
