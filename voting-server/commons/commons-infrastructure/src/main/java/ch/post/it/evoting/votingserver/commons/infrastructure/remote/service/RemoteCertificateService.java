/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.service;

import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;

/**
 * Service for retrieving certificates that are not stored in a different deployment (meaning that they are accessible only by calling another
 * microservice).
 */
public interface RemoteCertificateService {

	/**
	 * Gets the public certificate of an admin board
	 *
	 * @param id identifier of the admin board whose certificate is being searched
	 * @return the admin board Certificate
	 * @throws RetrofitException
	 */
	CertificateEntity getAdminBoardCertificate(String id) throws RetrofitException;

	/**
	 * Gets the tenant CA certificate
	 *
	 * @param tenantId - tenant
	 * @return
	 * @throws RetrofitException
	 */
	CertificateEntity getTenantCACertificate(String tenantId) throws RetrofitException;

}
