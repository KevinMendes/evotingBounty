/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.producer;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;

import retrofit2.Retrofit;

/**
 * "Producer" class that centralizes instantiation of all (retrofit) remote service client interfaces
 */
public class RemoteClientProducer {

	private RemoteClientProducer() {
		// Intentionally left blank.
	}

	public static <T> T createRestClient(String url, Class<T> clazz) {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClientWithJacksonConverter(url);
		return client.create(clazz);
	}
}
