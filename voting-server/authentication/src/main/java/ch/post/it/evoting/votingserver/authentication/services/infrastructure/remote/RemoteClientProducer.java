/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;

import retrofit2.Retrofit;

/**
 * "Producer" class that centralizes instantiation of all (retrofit) remote service client interfaces
 */
public class RemoteClientProducer {

	private static final String URI_ELECTION_INFORMATION = System.getenv("ELECTION_INFORMATION_CONTEXT_URL");
	private static final String VOTER_MATERIAL_CONTEXT_URL = System.getenv("VOTER_MATERIAL_CONTEXT_URL");

	private static <T> T createRestClient(String url, Class<T> clazz) {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(url);
		return client.create(clazz);
	}

	@Produces
	ElectionInformationClient electionInformationClient() {
		return createRestClient(URI_ELECTION_INFORMATION, ElectionInformationClient.class);
	}

	@Produces
	VoterMaterialClient voterMaterialClient() {
		return createRestClient(VOTER_MATERIAL_CONTEXT_URL, VoterMaterialClient.class);
	}
}
