/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;

/**
 * This is a handler of all the exception which are thrown, but not handler by the other existing handlers for specific types of exceptions.
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

	// Label for the message system is not available.
	private static final String ERROR_MESSAGE_SYSTEM_NOT_AVAILABLE = "system.is.not.available";

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	/**
	 * Generates an internal server error as a response of an uncontrolled exception.
	 *
	 * @param e - the exception.
	 * @return the generated response.
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)
	 */
	@Override
	@Produces(MediaType.APPLICATION_JSON)
	public Response toResponse(Exception e) {
		LOGGER.error("An error ocurred: ", e);

		String errorCode = "to be define!";
		Message message = new Message();
		message.setText(ERROR_MESSAGE_SYSTEM_NOT_AVAILABLE);
		message.addError("", "", errorCode);

		return Response.status(Status.NOT_FOUND).entity(message).build();
	}
}
