/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheck;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckRegistry;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckStatus;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckValidationType;

@RunWith(MockitoJUnitRunner.class)
public class CheckHealthResourceTest extends JerseyTest {

	@Mock
	HealthCheckRegistry healthCheckRegistry;

	@Override
	protected Application configure() {
		forceSet(TestProperties.CONTAINER_PORT, "0"); // allow parallel testing

		MockitoAnnotations.initMocks(this);

		// injection
		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(healthCheckRegistry).to(HealthCheckRegistry.class);
			}
		};
		CheckHealthResource checkHealthResource = new CheckHealthResource();
		return new ResourceConfig().register(checkHealthResource) // our controller
				.register(binder) // injection support
				.register(JacksonJsonProvider.class); // json writer
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void whenHealthChecksAreHealthyShouldReturnOK() {
		// given
		HashMap<HealthCheckValidationType, HealthCheck.HealthCheckResult> healthCheckResults = new HashMap<>();
		healthCheckResults.put(HealthCheckValidationType.DATABASE, HealthCheck.HealthCheckResult.healthy());
		HealthCheckStatus status = new HealthCheckStatus(healthCheckResults);
		when(healthCheckRegistry.runAllChecks()).thenReturn(status);

		// when
		final Response response = target("check").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		// then
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	@Test
	public void whenHealthAnyCheckIsUnHealthyShouldReturnServiceUnavailable() {
		// given
		HashMap<HealthCheckValidationType, HealthCheck.HealthCheckResult> healthCheckResults = new HashMap<>();
		healthCheckResults.put(HealthCheckValidationType.DATABASE, HealthCheck.HealthCheckResult.healthy());
		healthCheckResults.put(HealthCheckValidationType.LOGGING_INITIALIZED, HealthCheck.HealthCheckResult.unhealthy("unhealthy check"));
		HealthCheckStatus status = new HealthCheckStatus(healthCheckResults);
		when(healthCheckRegistry.runAllChecks()).thenReturn(status);

		// when
		final Response response = target("check").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		// then
		Assert.assertEquals(Response.Status.SERVICE_UNAVAILABLE.getStatusCode(), response.getStatus());
	}

	@Test
	public void testIfServiceRunning() {
		// given
		HashMap<HealthCheckValidationType, HealthCheck.HealthCheckResult> healthCheckResults = new HashMap<>();
		healthCheckResults.put(HealthCheckValidationType.DATABASE, HealthCheck.HealthCheckResult.healthy());
		HealthCheckStatus status = new HealthCheckStatus(healthCheckResults);
		List<HealthCheckValidationType> validationsToSkip = Collections.singletonList(HealthCheckValidationType.LOGGING_INITIALIZED);
		when(healthCheckRegistry.runChecksDifferentFrom(validationsToSkip)).thenReturn(status);

		// when
		final Response response = target("check").path("ready").request().accept(MediaType.APPLICATION_JSON_TYPE).get();

		// then
		Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

}
