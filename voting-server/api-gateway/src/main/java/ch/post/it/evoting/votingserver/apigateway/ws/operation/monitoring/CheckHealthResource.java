/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;

/**
 * REST Service for connectivity validation
 */
@Stateless(name = "agCheckHealth")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(CheckHealthResource.RESOURCE_PATH)
public class CheckHealthResource {

	static final String RESOURCE_PATH = "/check";

	private static final String READY_PATH = "ready";

	/**
	 * Check the health of a of resource.
	 *
	 * @return Returns a 200 response code, if the context is initialized properly.
	 */

	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(READY_PATH)
	public Response isApplicationRunning() {
		return Response.ok().build();
	}

	/**
	 * Check the health of a resource. Maintained for backwards compatibility
	 *
	 * @return Returns a 200 response code, if the context is initialized properly.
	 */

	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response check() {
		return Response.ok().build();
	}
}
