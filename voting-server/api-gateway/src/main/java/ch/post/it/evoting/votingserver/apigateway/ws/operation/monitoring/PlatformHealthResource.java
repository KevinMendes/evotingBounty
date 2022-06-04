/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.monitoring.HealthMonitoringClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheck;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckStatus;
import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckValidationType;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * Web service to monitor the health of the different microservices (contexts)
 */
@Stateless(name = "ag-PlatformHealthResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(RestApplication.API_OV_MONITORING_VERSION_BASEURI)
public class PlatformHealthResource {

	static final String CHECK_PLATFORM_HEALTH = "/check";
	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformHealthResource.class);
	private final Map<String, HealthMonitoringClient> clientMap = new HashMap<>();
	@Inject
	@Named(Constants.AU)
	private HealthMonitoringClient authenticationMonitoringClient;
	@Inject
	@Named(Constants.EA)
	private HealthMonitoringClient extendedAuthenticationMonitoringClient;
	@Inject
	@Named(Constants.EI)
	private HealthMonitoringClient electionInformationMontoringClient;
	@Inject
	@Named(Constants.VM)
	private HealthMonitoringClient voterMaterialMonitoringClient;
	@Inject
	@Named(Constants.VV)
	private HealthMonitoringClient voteVerificationMonitoringClient;
	@Inject
	@Named(Constants.VW)
	private HealthMonitoringClient votingWorkflowMonitoringClient;
	@Inject
	@Named(Constants.CR)
	private HealthMonitoringClient certificateRegistryMonitoringClient;
	@Inject
	@Named(Constants.OR)
	private HealthMonitoringClient orchestratorMonitoringClient;
	@Inject
	@Named(Constants.MO)
	private HealthMonitoringClient messageBrokerOrchestratorMonitoringClient;

	/**
	 * Initializes clients.
	 */
	@PostConstruct
	void initializeClientMap() {
		clientMap.put(Constants.AU, authenticationMonitoringClient);
		clientMap.put(Constants.EA, extendedAuthenticationMonitoringClient);
		clientMap.put(Constants.EI, electionInformationMontoringClient);
		clientMap.put(Constants.VM, voterMaterialMonitoringClient);
		clientMap.put(Constants.VV, voteVerificationMonitoringClient);
		clientMap.put(Constants.VW, votingWorkflowMonitoringClient);
		clientMap.put(Constants.CR, certificateRegistryMonitoringClient);
		clientMap.put(Constants.OR, orchestratorMonitoringClient);
		clientMap.put(Constants.MO, messageBrokerOrchestratorMonitoringClient);
	}

	/**
	 * Performs a check for all contexts.
	 *
	 * @return Returns a 200 response code with the result of the status of all contexts.
	 */
	@Path(CHECK_PLATFORM_HEALTH)
	@GET
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response checkPlatformHealth() {
		// for each context/client check the context health, in parallel if possible.
		Map<String, HealthCheckStatus> statusMap = clientMap.entrySet().stream().parallel()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> check(e.getKey(), e.getValue())));

		// if we encounter problems serializing the statuses map we can use GenericEntity to help with
		// the serialization
		// and we can also use it in the client
		return Response.ok().entity(statusMap).build();
	}

	private HealthCheckStatus check(String contextName, HealthMonitoringClient client) {
		try {
			retrofit2.Response<HealthCheckStatus> executeCall = RetrofitConsumer.executeCall(client.checkHealth());
			return executeCall.body();
		} catch (RetrofitException e) {
			return tryHandleAsHealthCheck(e, contextName);
		}
	}

	private HealthCheckStatus tryHandleAsHealthCheck(final RetrofitException rfE, final String contextName) {
		if (rfE.getHttpCode() == 503 && rfE.getErrorBody() != null && rfE.getErrorBody().contentLength() != 0) {
			final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
			try (InputStream errorStream = rfE.getErrorBody().byteStream()) {
				return objectMapper.readValue(errorStream, HealthCheckStatus.class);
			} catch (IOException ioE) {
				LOGGER.error(ioE.getMessage(), ioE);
				LOGGER.error(String.format("Error trying to consume error message for checking the health of context %s.", contextName));
			}
		} else {
			LOGGER.error("Error trying to call to end-point for checking the health of context {}.", contextName);
		}
		LOGGER.error("Error trying to call to end-point for checking the health of context {}.", contextName);
		return new HealthCheckStatus(
				Collections.singletonMap(HealthCheckValidationType.STATUS, HealthCheck.HealthCheckResult.unhealthy(rfE.getMessage())));
	}
}
