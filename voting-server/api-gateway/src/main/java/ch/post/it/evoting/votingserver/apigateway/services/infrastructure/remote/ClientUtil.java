/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;

import retrofit2.Retrofit;

public final class ClientUtil {
	private ClientUtil() {
		super();
	}

	public static <T> T createRestClient(String url, Class<T> clazz) {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(url);
		return client.create(clazz);
	}
}
