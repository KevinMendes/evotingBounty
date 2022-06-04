/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.producer;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

public class MessageBrokerOrchestratorRemoteClientProducer {

	public static final String URI_MESSAGE_BROKER_ORCHESTRATOR = System.getenv("MESSAGE_BROKER_ORCHESTRATOR_CONTEXT_URL");

	@Produces
	MessageBrokerOrchestratorClient ccOrchestratorClient() {
		return RemoteClientProducer.createRestClient(URI_MESSAGE_BROKER_ORCHESTRATOR, MessageBrokerOrchestratorClient.class);
	}
}
