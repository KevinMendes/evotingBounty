/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.monitoring;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.ClientUtil;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * "Producer" class that centralizes instantiation of all HealthCheck client interfaces
 */
public class HealthMonitoringClientProducer {

	private static final String URI_AUTHENTICATION = System.getenv("AUTHENTICATION_CONTEXT_URL");
	private static final String URI_EXTENDED_AUTH_CONTEXT = System.getenv("EXTENDED_AUTHENTICATION_CONTEXT_URL");
	private static final String URI_ELECTION_INFORMATION = System.getenv("ELECTION_INFORMATION_CONTEXT_URL");
	private static final String URI_VOTER_MATERIAL = System.getenv("VOTER_MATERIAL_CONTEXT_URL");
	private static final String URI_VOTE_VERIFICATION = System.getenv("VERIFICATION_CONTEXT_URL");
	private static final String URI_VOTING_WORKFLOW = System.getenv("VOTING_WORKFLOW_CONTEXT_URL");
	private static final String URI_CERTIFICATE_REGISTRY = System.getenv("CERTIFICATES_CONTEXT_URL");
	private static final String URI_ORCHESTRATOR_CONTEXT = System.getenv("ORCHESTRATOR_CONTEXT_URL");
	private static final String URI_MESSAGE_BROKER_ORCHESTRATOR_CONTEXT = System.getenv("MESSAGE_BROKER_ORCHESTRATOR_CONTEXT_URL");

	@Produces
	@Named(Constants.AU)
	HealthMonitoringClient authenticationMonitoringClient() {
		return ClientUtil.createRestClient(URI_AUTHENTICATION, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.EA)
	HealthMonitoringClient extendedAuthenticationMonitoringClient() {
		return ClientUtil.createRestClient(URI_EXTENDED_AUTH_CONTEXT, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.EI)
	HealthMonitoringClient electionInformationMontoringClient() {
		return ClientUtil.createRestClient(URI_ELECTION_INFORMATION, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.VM)
	HealthMonitoringClient voterMaterialMonitoringClient() {
		return ClientUtil.createRestClient(URI_VOTER_MATERIAL, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.VV)
	HealthMonitoringClient voteVerificationMonitoringClient() {
		return ClientUtil.createRestClient(URI_VOTE_VERIFICATION, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.VW)
	HealthMonitoringClient votingWorkflowMontoringClient() {
		return ClientUtil.createRestClient(URI_VOTING_WORKFLOW, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.CR)
	HealthMonitoringClient certificateRegistryMonitoringClient() {
		return ClientUtil.createRestClient(URI_CERTIFICATE_REGISTRY, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.OR)
	HealthMonitoringClient orchestratorMonitoringClient() {
		return ClientUtil.createRestClient(URI_ORCHESTRATOR_CONTEXT, HealthMonitoringClient.class);
	}

	@Produces
	@Named(Constants.MO)
	HealthMonitoringClient messageBrokerOrchestratorMonitoringClient() {
		return ClientUtil.createRestClient(URI_MESSAGE_BROKER_ORCHESTRATOR_CONTEXT, HealthMonitoringClient.class);
	}

}
