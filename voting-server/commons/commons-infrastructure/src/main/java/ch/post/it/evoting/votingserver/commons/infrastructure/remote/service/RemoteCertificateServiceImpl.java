/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.service;

import javax.annotation.PostConstruct;

import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.CertificateServiceClient;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;

import retrofit2.Retrofit;

/**
 * Service implementation for retrieving certificates that are not stored in a different deployment (meaning that they are accessible only by calling
 * another microservice).
 */
public class RemoteCertificateServiceImpl implements RemoteCertificateService {

	public static final String CERTIFICATES_CONTEXT_URL_PROPERTY = "CERTIFICATES_CONTEXT_URL";

	private static final String URI_CERTIFICATE_REGISTRY = System.getenv(CERTIFICATES_CONTEXT_URL_PROPERTY);

	private static final String PATH_CERTIFICATES = "certificates/public";

	private static final String PATH_TENANT_DATA = "tenantdata";

	private CertificateServiceClient certificateServiceClient;

	@PostConstruct
	public void intializeElectionInformationAdminClient() {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(URI_CERTIFICATE_REGISTRY);
		certificateServiceClient = client.create(CertificateServiceClient.class);
	}

	@Override
	public CertificateEntity getAdminBoardCertificate(String id) throws RetrofitException {

		return RetrofitConsumer.processResponse(certificateServiceClient.getAdminCertificate(PATH_CERTIFICATES, id));

	}

	@Override
	public CertificateEntity getTenantCACertificate(String tenantId) throws RetrofitException {
		return RetrofitConsumer.processResponse(certificateServiceClient.getTenantCACertificate(PATH_TENANT_DATA, tenantId));
	}

}
