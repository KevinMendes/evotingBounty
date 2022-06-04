/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;

/**
 * Handler responsible of catching the Duplicate entry insertions in order to provide a proper error message
 */
@Provider
public class DuplicateEntryExceptionHandler implements ExceptionMapper<DuplicateEntryException> {

	// Label for the message system is not available.
	private static final String WARN_MESSAGE_DUPLICATE_ENTRY = "Duplicate entry!";

	private static final Logger LOGGER = LoggerFactory.getLogger(DuplicateEntryExceptionHandler.class);

	/**
	 * Generates a not found as a response of an ResourceNotFoundException.
	 *
	 * @param e - the exception.
	 * @return the generated response.
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)
	 */
	@Override
	@Produces(MediaType.APPLICATION_JSON)
	public Response toResponse(DuplicateEntryException e) {
		LOGGER.warn("An error ocurred: ", e);

		String errorCode = ErrorCodes.DUPLICATE_ENTRY;
		Message message = new Message();
		message.setText(WARN_MESSAGE_DUPLICATE_ENTRY);
		message.addError("", "", errorCode);
		return Response.status(Response.Status.OK).entity(message).build();
	}
}
