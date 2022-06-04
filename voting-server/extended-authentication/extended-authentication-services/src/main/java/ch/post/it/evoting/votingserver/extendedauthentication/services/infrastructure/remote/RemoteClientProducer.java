/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.VoterMaterialServiceClient;

import retrofit2.Retrofit;

/**
 * "Producer" class that centralizes instantiation of all (retrofit) remote service client interfaces
 */
public class RemoteClientProducer {

	private static final String AUTHENTICATION_CONTEXT_URL = System.getenv("AUTHENTICATION_CONTEXT_URL");
	private static final String VOTER_MATERIAL_CONTEXT_URL = System.getenv("VOTER_MATERIAL_CONTEXT_URL");

	private static <T> T createRestClient(String url, Class<T> clazz) {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(url);
		return client.create(clazz);
	}

	@Produces
	AuthenticationClient authenticationClient() {
		return createRestClient(AUTHENTICATION_CONTEXT_URL, AuthenticationClient.class);
	}

	@Produces
	VoterMaterialServiceClient voterMaterialServiceClient() {
		return createRestClient(VOTER_MATERIAL_CONTEXT_URL, VoterMaterialServiceClient.class);
	}
}
