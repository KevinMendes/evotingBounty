/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.monitoring.HealthMonitoringClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheck;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckStatus;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckValidationType;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;

@RunWith(MockitoJUnitRunner.class)
public class PlatformHealthResourceTest extends JerseyTest {

	private static final String URL_PLATFORM_HEALTH_CONTEXT_DATA =
			RestApplication.API_OV_MONITORING_VERSION_BASEURI + PlatformHealthResource.CHECK_PLATFORM_HEALTH;
	private static final String VERSION = "1.2";

	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();

	@Mock
	private Logger logger;

	@Mock
	private HealthMonitoringClient authenticationMonitoringClient;

	@Mock
	private HealthMonitoringClient extendedAuthenticationMonitoringClient;

	@Mock
	private HealthMonitoringClient electionInformationMontoringClient;

	@Mock
	private HealthMonitoringClient voterMaterialMontoringClient;

	@Mock
	private HealthMonitoringClient voteVerificationMontoringClient;

	@Mock
	private HealthMonitoringClient votingWorkflowMontoringClient;

	@Mock
	private HealthMonitoringClient certificateRegistryMonitoringClient;

	@Mock
	private HealthMonitoringClient mixDecMonitoringClient;

	@Mock
	private HealthMonitoringClient orchestratorMonitoringClient;

	@Mock
	private HealthMonitoringClient messageBrokerOrchestratorMonitoringClient;

	@InjectMocks
	private PlatformHealthResource sut;

	@Test
	public void checkPlatformHealthWhenHealthy() throws Exception {

		int mockedInvocationStatus = 200;

		@SuppressWarnings("unchecked")
		Call<HealthCheckStatus> callMock = (Call<HealthCheckStatus>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(createHealthCheckResponse()));

		when(authenticationMonitoringClient.checkHealth()).thenReturn(callMock);
		when(extendedAuthenticationMonitoringClient.checkHealth()).thenReturn(callMock);
		when(electionInformationMontoringClient.checkHealth()).thenReturn(callMock);
		when(voteVerificationMontoringClient.checkHealth()).thenReturn(callMock);
		when(votingWorkflowMontoringClient.checkHealth()).thenReturn(callMock);
		when(certificateRegistryMonitoringClient.checkHealth()).thenReturn(callMock);
		when(voterMaterialMontoringClient.checkHealth()).thenReturn(callMock);
		when(orchestratorMonitoringClient.checkHealth()).thenReturn(callMock);
		when(messageBrokerOrchestratorMonitoringClient.checkHealth()).thenReturn(callMock);

		Response response = target(URL_PLATFORM_HEALTH_CONTEXT_DATA).resolveTemplate("version", VERSION).request().get();
		int status = response.getStatus();

		final Map<String, HealthCheckStatus> actualStatusMap = response.readEntity(new GenericType<HashMap<String, HealthCheckStatus>>() {
		});
		Assert.assertEquals(mockedInvocationStatus, status);

		// everything _is_ healthy
		Assert.assertTrue(actualStatusMap.values().stream().allMatch(HealthCheckStatus::isHealthy));
	}

	@Test
	public void checkPlatformHealthWhenUnHealthy() throws Exception {

		int mockedInvocationStatus = 200;

		@SuppressWarnings("unchecked")
		Call<HealthCheckStatus> callMock = (Call<HealthCheckStatus>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(createFailedHealthCheckResponse()));

		when(authenticationMonitoringClient.checkHealth()).thenReturn(callMock);
		when(extendedAuthenticationMonitoringClient.checkHealth()).thenReturn(callMock);
		when(electionInformationMontoringClient.checkHealth()).thenReturn(callMock);
		when(voteVerificationMontoringClient.checkHealth()).thenReturn(callMock);
		when(votingWorkflowMontoringClient.checkHealth()).thenReturn(callMock);
		when(certificateRegistryMonitoringClient.checkHealth()).thenReturn(callMock);
		when(voterMaterialMontoringClient.checkHealth()).thenReturn(callMock);
		when(orchestratorMonitoringClient.checkHealth()).thenReturn(callMock);
		when(messageBrokerOrchestratorMonitoringClient.checkHealth()).thenReturn(callMock);

		Response response = target(URL_PLATFORM_HEALTH_CONTEXT_DATA).resolveTemplate("version", VERSION).request().get();
		int status = response.getStatus();

		Assert.assertEquals(mockedInvocationStatus, status);

		final Map<String, HealthCheckStatus> actualStatusMap = response.readEntity(new GenericType<HashMap<String, HealthCheckStatus>>() {
		});
		Assert.assertEquals(mockedInvocationStatus, status);

		// everything _is not_ healthy
		Assert.assertTrue(actualStatusMap.values().stream().noneMatch(HealthCheckStatus::isHealthy));

	}

	private HealthCheckStatus createHealthCheckResponse() {
		return new HealthCheckStatus(
				Collections.singletonMap(HealthCheckValidationType.LOGGING_INITIALIZED, HealthCheck.HealthCheckResult.healthy()));
	}

	private HealthCheckStatus createFailedHealthCheckResponse() {

		return new HealthCheckStatus(Collections
				.singletonMap(HealthCheckValidationType.LOGGING_INITIALIZED, HealthCheck.HealthCheckResult.unhealthy("something is wrong")));
	}

	@Override
	protected Application configure() {

		AbstractBinder binder = new AbstractBinder() {
			@Override
			protected void configure() {
				bind(logger).to(Logger.class);
				bind(authenticationMonitoringClient).to(HealthMonitoringClient.class).named(Constants.AU);
				bind(extendedAuthenticationMonitoringClient).to(HealthMonitoringClient.class).named(Constants.EA);
				bind(electionInformationMontoringClient).to(HealthMonitoringClient.class).named(Constants.EI);
				bind(voterMaterialMontoringClient).to(HealthMonitoringClient.class).named(Constants.VM);
				bind(voteVerificationMontoringClient).to(HealthMonitoringClient.class).named(Constants.VV);
				bind(votingWorkflowMontoringClient).to(HealthMonitoringClient.class).named(Constants.VW);
				bind(certificateRegistryMonitoringClient).to(HealthMonitoringClient.class).named(Constants.CR);
				bind(orchestratorMonitoringClient).to(HealthMonitoringClient.class).named(Constants.OR);
				bind(messageBrokerOrchestratorMonitoringClient).to(HealthMonitoringClient.class).named(Constants.MO);
			}
		};

		forceSet(TestProperties.CONTAINER_PORT, "0");
		return new ResourceConfig().register(PlatformHealthResource.class).register(binder);
	}
}
