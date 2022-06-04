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

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;

/**
 * Handler for RetrofitError
 */
@Provider
public class RetrofitErrorHandler implements ExceptionMapper<RetrofitException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RetrofitErrorHandler.class);

	/**
	 * Converts a RetrofitError to a JAX-RS Response If the RetrofitError contains a body, this body is also returned.
	 *
	 * @param e - the RetrofitException.
	 * @return the converted response.
	 * @see javax.ws.rs.ext.ExceptionMapper#toResponse(Throwable)
	 */
	@Override
	@Produces(MediaType.APPLICATION_JSON)
	public Response toResponse(RetrofitException e) {
		LOGGER.error("An error occurred: ", e);

		// there are errors that may not contain a body, for example, 401 and others. so, check for null
		// before toString
		Object body = e.getErrorBody();
		if (body != null) {
			return Response.status(e.getHttpCode()).entity(body.toString()).build();
		} else {
			return Response.status(e.getHttpCode()).build();
		}
	}
}
