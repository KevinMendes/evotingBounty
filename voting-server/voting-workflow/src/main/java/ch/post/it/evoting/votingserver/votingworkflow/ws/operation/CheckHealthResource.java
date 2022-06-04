/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

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
 * REST Service for connectivity validation purposes
 */
@Stateless(name = "vwCheckHealth")
@Path("check")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CheckHealthResource {

	private static final String READY_PATH = "ready";
	@Inject
	private HealthCheckRegistry healthCheckRegistry;

	/**
	 * Runs all registered Health checks.
	 *
	 * @return Returns an HTTP Status code 200, if all health checks are ok, 503 if any has failed
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		final HealthCheckStatus serviceStatus = healthCheckRegistry.runAllChecks();
		boolean isSystemHealthy = serviceStatus.isHealthy();
		if (isSystemHealthy) {
			return Response.ok().entity(serviceStatus).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(serviceStatus).build();
		}
	}

	/**
	 * Runs the health checks that simply identify whether the application is running.
	 * <p>
	 * Despite being useful for validating the state of the application, some health checks (logging
	 * initialized or not) aren't necessary to identify if the service has been properly deployed.
	 *
	 * @return Returns an HTTP Status code 200, if all health checks are ok, 503 if any has failed
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(READY_PATH)
	public Response isApplicationRunning() {

		final HealthCheckStatus serviceStatus = healthCheckRegistry
				.runChecksDifferentFrom(Arrays.asList(HealthCheckValidationType.LOGGING_INITIALIZED));

		boolean isSystemHealthy = serviceStatus.isHealthy();
		if (isSystemHealthy) {
			return Response.ok().entity(serviceStatus).build();
		} else {
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(serviceStatus).build();
		}
	}
}
