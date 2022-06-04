/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;

import retrofit2.Retrofit;

/**
 * "Producer" class that centralizes instantiation of all (retrofit) remote service client interfaces
 */
public class RemoteClientProducer {

	private static final String URI_ELECTION_INFORMATION = System.getenv("ELECTION_INFORMATION_CONTEXT_URL");
	private static final String AUTHENTICATION_CONTEXT_URL = System.getenv("AUTHENTICATION_CONTEXT_URL");
	private static final String VERIFICATION_CONTEXT_URL = System.getenv("VERIFICATION_CONTEXT_URL");

	private static <T> T createRestClient(String url, Class<T> clazz) {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(url);
		return client.create(clazz);
	}

	@Produces
	ElectionInformationClient electionInformationClient() {
		return createRestClient(URI_ELECTION_INFORMATION, ElectionInformationClient.class);
	}

	@Produces
	AuthenticationClient authenticationClient() {
		return createRestClient(AUTHENTICATION_CONTEXT_URL, AuthenticationClient.class);
	}

	@Produces
	VerificationClient verificationClient() {
		return createRestClient(VERIFICATION_CONTEXT_URL, VerificationClient.class);
	}
}
