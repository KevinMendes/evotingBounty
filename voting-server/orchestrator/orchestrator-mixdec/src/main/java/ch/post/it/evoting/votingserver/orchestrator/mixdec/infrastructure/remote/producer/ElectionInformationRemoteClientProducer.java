/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.producer;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ElectionInformationClient;

public class ElectionInformationRemoteClientProducer extends RemoteClientProducer {

	private static final String URI_ELECTION_INFORMATION = System.getenv("ELECTION_INFORMATION_CONTEXT_URL");

	@Produces
	ElectionInformationClient electionInformationClient() {
		return createRestClient(URI_ELECTION_INFORMATION, ElectionInformationClient.class);
	}
}
