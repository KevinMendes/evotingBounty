/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.exceptions;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.HTTPStatus;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.Message;

/**
 * This class handles the exceptions thrown by the applications and returns a proper Error Response in Json format.
 */
@Provider
public class ApplicationExceptionHandler implements ExceptionMapper<ApplicationException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

	/**
	 * Generates an unprocessable entity as a response of an ApplicationException exception.
	 *
	 * @param e - the exception.
	 * @return the generated response.
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)
	 */
	@Override
	@Produces(MediaType.APPLICATION_JSON)
	public Response toResponse(ApplicationException e) {
		LOGGER.error("An error ocurred: ", e);

		Message message = new Message();
		message.setText(e.getMessage());

		String errorCode = e.getErrorCode();
		if (StringUtils.isEmpty(errorCode)) {
			errorCode = "";
		}

		String resource = e.getResource();
		if (StringUtils.isEmpty(resource)) {
			resource = "";
		}

		String field = e.getField();
		if (StringUtils.isEmpty(field)) {
			field = "";
		}

		message.addError(resource, field, errorCode);
		return Response.status(HTTPStatus.UNPROCESSABLE_ENTITY.getValue()).entity(message).build();
	}
}
