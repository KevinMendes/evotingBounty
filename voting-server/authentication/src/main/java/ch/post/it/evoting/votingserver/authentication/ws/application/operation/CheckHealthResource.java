/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import java.util.Arrays;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckRegistry;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckStatus;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckValidationType;

/**
 * REST Service for service health check
 */
@Stateless(name = "auCheckHealth")
@Path("check")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CheckHealthResource {

	public static final String READY = "ready";

	@Inject
	private HealthCheckRegistry healthCheckRegistry;

	/**
	 * Run all registered Health checks
	 *
	 * @return Returns an HTTP Status code 200, if all health checks are ok, 503 if any has failed
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {

		final HealthCheckStatus serviceStatus = healthCheckRegistry.runAllChecks();
		final boolean isSystemHealthy = serviceStatus.isHealthy();
		if (isSystemHealthy) {
			return Response.ok().entity(serviceStatus).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(serviceStatus).build();
		}
	}

	/**
	 * Run the health checks with the exception of checking the initialization of the logging
	 *
	 * @return Returns an HTTP Status code 200, if the health checks are ok, 503 if any has failed
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(READY)
	public Response isApplicationRunning() {

		final HealthCheckStatus serviceStatus = healthCheckRegistry
				.runChecksDifferentFrom(Arrays.asList(HealthCheckValidationType.LOGGING_INITIALIZED));

		final boolean isSystemHealthy = serviceStatus.isHealthy();
		if (isSystemHealthy) {
			return Response.ok().entity(serviceStatus).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(serviceStatus).build();
		}
	}

}
