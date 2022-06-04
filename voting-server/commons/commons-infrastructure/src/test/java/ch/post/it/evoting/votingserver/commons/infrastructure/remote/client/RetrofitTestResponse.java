/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import javax.validation.constraints.NotNull;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RetrofitTestResponse {

	@GET(".")
	Call<ResponseBody> getResponse();

	@GET("withQueryParams/{query_param_1}/{query_param_2}")
	Call<ResponseBody> getResponseWithQueryParams(
			@Path("query_param_1")
					String queryParam1,
			@Path("query_param_2")
					String queryParam2);

	@POST("postString")
	Call<ResponseBody> postString(
			@NotNull
			@Body
					RequestBody body);

	@POST("testPojo")
	Call<ResponseBody> postPojo(
			@NotNull
			@Body
					TestPojo testPojo);

}
