/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.clients;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AdminPortalClient {

	@GET("export/tenants/{tenantId}")
	Call<ResponseBody> export(
			@Path("tenantId")
			final String tenantId);

}
