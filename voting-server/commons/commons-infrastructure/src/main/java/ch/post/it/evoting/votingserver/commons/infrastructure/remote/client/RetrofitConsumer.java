/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

public class RetrofitConsumer {

	private RetrofitConsumer() {

	}

	public static <T> Response<T> executeCall(final Call<T> call) throws RetrofitException {
		final Response<T> execute;
		try {
			execute = call.execute();
		} catch (final IOException e) {
			throw new RetrofitException(404, e.getMessage(), e);
		}
		if (!execute.isSuccessful()) {
			throw new RetrofitException(execute.code(), execute.errorBody());
		}
		return execute;
	}

	public static <T> T processResponse(final Call<T> call) throws RetrofitException {
		final Response<T> execute = executeCall(call);
		return execute.body();
	}
}
