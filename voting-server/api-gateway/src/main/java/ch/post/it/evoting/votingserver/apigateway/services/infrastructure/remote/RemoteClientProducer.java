/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.AuthenticationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.CertificateServiceClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoterMaterialAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents.MessageBrokerOrchestratorClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.controlcomponents.OrchestratorClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.ExtendedAuthenticationVotingClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.voting.VotingWorkflowVotingClient;

/**
 * "Producer" class that centralizes instantiation of all (retrofit) remote service client interfaces
 */
public class RemoteClientProducer {

	private static final String URI_CERTIFICATES_SERVICE = System.getenv("CERTIFICATES_CONTEXT_URL");

	private static final String URI_AUTHENTICATION_CONTEXT = System.getenv("AUTHENTICATION_CONTEXT_URL");

	private static final String URI_ELECTION_INFORMATION = System.getenv("ELECTION_INFORMATION_CONTEXT_URL");

	private static final String URI_VERIFICATION_CONTEXT = System.getenv("VERIFICATION_CONTEXT_URL");

	private static final String VOTER_MATERIAL_CONTEXT_URL = System.getenv("VOTER_MATERIAL_CONTEXT_URL");

	private static final String URI_EXT_AUTH_CONTEXT = System.getenv("EXTENDED_AUTHENTICATION_CONTEXT_URL");

	private static final String URI_VOTING_WORKFLOW_CONTEXT = System.getenv("VOTING_WORKFLOW_CONTEXT_URL");

	private static final String URI_ORCHESTRATOR_CONTEXT = System.getenv("ORCHESTRATOR_CONTEXT_URL");

	private static final String URI_MESSAGE_BROKER_ORCHESTRATOR_CONTEXT = System.getenv("MESSAGE_BROKER_ORCHESTRATOR_CONTEXT_URL");

	@Produces
	CertificateServiceClient certificateServiceClient() {
		return ClientUtil.createRestClient(URI_CERTIFICATES_SERVICE, CertificateServiceClient.class);
	}

	@Produces
	AuthenticationAdminClient authenticationAdminClient() {
		return ClientUtil.createRestClient(URI_AUTHENTICATION_CONTEXT, AuthenticationAdminClient.class);
	}

	@Produces
	ElectionInformationAdminClient electionInformationAdminClient() {
		return ClientUtil.createRestClient(URI_ELECTION_INFORMATION, ElectionInformationAdminClient.class);
	}

	@Produces
	VoteVerificationAdminClient voteVerificationAdminClient() {
		return ClientUtil.createRestClient(URI_VERIFICATION_CONTEXT, VoteVerificationAdminClient.class);
	}

	@Produces
	VoterMaterialAdminClient voterMaterialAdminClient() {
		return ClientUtil.createRestClient(VOTER_MATERIAL_CONTEXT_URL, VoterMaterialAdminClient.class);
	}

	@Produces
	ExtendedAuthenticationVotingClient extendedAuthenticationVotingClient() {
		return ClientUtil.createRestClient(URI_EXT_AUTH_CONTEXT, ExtendedAuthenticationVotingClient.class);
	}

	@Produces
	VotingWorkflowVotingClient votingWorkflowVotingClient() {
		return ClientUtil.createRestClient(URI_VOTING_WORKFLOW_CONTEXT, VotingWorkflowVotingClient.class);
	}

	@Produces
	OrchestratorClient orchestratorClient() {
		return ClientUtil.createRestClient(URI_ORCHESTRATOR_CONTEXT, OrchestratorClient.class);
	}

	@Produces
	MessageBrokerOrchestratorClient messageBrokerOrchestratorClient() {
		return ClientUtil.createRestClient(URI_MESSAGE_BROKER_ORCHESTRATOR_CONTEXT, MessageBrokerOrchestratorClient.class);
	}
}
