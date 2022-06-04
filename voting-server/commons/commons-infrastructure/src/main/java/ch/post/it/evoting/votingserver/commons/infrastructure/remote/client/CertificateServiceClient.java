/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CertificateServiceClient {

	String PARAMETER_PATH_CERTIFICATE_DATA = "pathCertificateData";
	String PARAMETER_PATH_TENANT_DATA = "pathTenantData";

	String PARAMETER_VALUE_ADMIN_BOARD_ID = "adminBoardId";
	String PARAMETER_VALUE_TENANT_ID = "tenantId";

	@GET("{pathCertificateData}/name/{adminBoardId}")
	Call<CertificateEntity> getAdminCertificate(
			@Path(value = PARAMETER_PATH_CERTIFICATE_DATA, encoded = true)
					String pathCertificateData,
			@Path(PARAMETER_VALUE_ADMIN_BOARD_ID)
					String adminBoardId);

	@GET("{pathTenantData}/tenant/{tenantId}")
	Call<CertificateEntity> getTenantCACertificate(
			@Path(value = PARAMETER_PATH_TENANT_DATA, encoded = true)
					String pathTenantData,
			@Path(PARAMETER_VALUE_TENANT_ID)
					String tenantId);

}
