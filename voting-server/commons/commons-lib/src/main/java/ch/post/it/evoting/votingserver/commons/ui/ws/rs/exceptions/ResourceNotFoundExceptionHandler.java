/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;

@Provider
public class ResourceNotFoundExceptionHandler implements ExceptionMapper<ResourceNotFoundException> {

	// Label for the message system is not available.
	private static final String ERROR_MESSAGE_NOT_FOUND = "Resource not found!";

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceNotFoundExceptionHandler.class);

	/**
	 * Generates a not found as a response of an ResourceNotFoundException.
	 *
	 * @param e - the exception.
	 * @return the generated response.
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)
	 */
	@Override
	public Response toResponse(final ResourceNotFoundException e) {
		LOGGER.error("An error ocurred: ", e);

		final String errorCode = ErrorCodes.RESOURCE_NOT_FOUND;
		final Message message = new Message();
		message.setText(ERROR_MESSAGE_NOT_FOUND);
		message.addError("", "", errorCode);
		return Response.status(Response.Status.NOT_FOUND).entity(message).build();
	}
}
