/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.HTTPStatus;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;

/**
 * Handler which will control the constraints defined for validation purposes
 */
@Provider
public class ValidationExceptionHandler implements ExceptionMapper<ValidationException> {

	private static final String ERROR_MESSAGE_INVALID_ENTITY = "The input data is not valid";

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationExceptionHandler.class);

	@Override
	public Response toResponse(ValidationException e) {
		LOGGER.error("An error ocurred: ", e);

		String errorCode = ErrorCodes.VALIDATION_EXCEPTION;
		Message message = new Message();
		message.setText(ERROR_MESSAGE_INVALID_ENTITY);
		message.addError("", "", errorCode);
		if (e instanceof SyntaxErrorException) {
			return Response.status(Status.BAD_REQUEST).build();
		} else if (e instanceof SemanticErrorException) {
			return Response.status(HTTPStatus.UNPROCESSABLE_ENTITY.getValue()).entity(message).build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}
}
