/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin;

import javax.validation.constraints.NotNull;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CertificateServiceClient {

	@POST("{pathCertificateData}/secured/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ResponseBody> saveCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Body
					RequestBody certificateData);

	@POST("{pathCertificateData}/secured/tenant/{tenantId}")
	Call<ResponseBody> saveCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Body
					RequestBody certificateData);

	@POST("{pathCertificateData}/secured")
	Call<JsonObject> saveCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			@Header(RestClientInterceptor.HEADER_ORIGINATOR)
					String originator,
			@NotNull
			@Header(RestClientInterceptor.HEADER_SIGNATURE)
					String signature,
			@NotNull
			@Body
					RequestBody certificateData);

	@GET("{pathCertificateData}/tenant/{tenantId}/electionevent/{electionEventId}/name/{certificateName}")
	Call<JsonObject> getCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_CERTIFICATE_NAME)
					String certificateName,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathCertificateData}/tenant/{tenantId}/name/{certificateName}")
	Call<JsonObject> getCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_CERTIFICATE_NAME)
					String certificateName,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathCertificateData}/tenant/{tenantId}/name/{certificateName}/status")
	Call<JsonObject> checkIfCertificateExists(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_CERTIFICATE_NAME)
					String certificateName,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

	@GET("{pathCertificateData}/name/{certificateName}")
	Call<JsonObject> getCertificate(
			@Path(value = Constants.PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(Constants.PARAMETER_VALUE_CERTIFICATE_NAME)
					String certificateName,
			@NotNull
			@Header(Constants.PARAMETER_X_FORWARDED_FOR)
					String xForwardedFor,
			@NotNull
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId);

}
