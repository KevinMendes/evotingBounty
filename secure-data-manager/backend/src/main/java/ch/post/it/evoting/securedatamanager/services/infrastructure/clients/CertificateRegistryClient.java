/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.clients;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpHeaders;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.domain.model.certificateRegistry.Certificate;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CertificateRegistryClient {

	@POST("certificates/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ResponseBody> saveCertificate(
			@Path(Constants.TENANT_ID)
			final String tenantId,
			@Path(Constants.ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			@Body
			final Certificate certificate);

	@Headers(HttpHeaders.CONTENT_TYPE + ": " + MediaType.APPLICATION_JSON)
	@POST("certificates/tenant/{tenantId}")
	Call<ResponseBody> saveCertificate(
			@Path(Constants.TENANT_ID)
			final String tenantId,
			@NotNull
			@Body
			final Certificate certificate);

	@POST("certificates")
	Call<ResponseBody> saveCertificate(
			@NotNull
			@Body
			final Certificate certificate);

}
