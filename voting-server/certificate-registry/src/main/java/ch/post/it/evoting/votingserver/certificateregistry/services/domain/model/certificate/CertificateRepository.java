/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling Certificates
 */
@Local
public interface CertificateRepository extends BaseRepository<CertificateEntity, Long> {

	/**
	 * Returns a certificate if one exists with the given name.
	 *
	 * @param certificateName - name of the certificate.
	 * @return a Certificate.
	 */
	CertificateEntity findByName(String certificateName) throws ResourceNotFoundException, DuplicateEntryException;

	/**
	 * Returns a certificate if one exists for the given parameters.
	 *
	 * @param tenantId        - tenantIdentifier.
	 * @param electionEventId - electionEventIdentifier.
	 * @param certificateName - certificate name.
	 * @return a Certificate.
	 */
	CertificateEntity findByTenanElectionEventAndCertificateName(String tenantId, String electionEventId, String certificateName)
			throws ResourceNotFoundException, DuplicateEntryException;

	/**
	 * Returns a certificate if one exists for the given parameters.
	 *
	 * @param tenantId        - tenantIdentifier.
	 * @param certificateName - certificate name.
	 * @return a Certificate.
	 */
	CertificateEntity findByTenantAndCertificateName(String tenantId, String certificateName)
			throws ResourceNotFoundException, DuplicateEntryException;

	/**
	 * Checks if a certificate exists
	 *
	 * @param tenantId - tenant identifier
	 * @param name     - name of the certificate
	 * @return 0 of not exists and the number of elements otherwise
	 */
	Long checkIfCertificateExist(String tenantId, String name);

	/**
	 * Saves a certificate into the repository.
	 *
	 * @param certificateEntity
	 */
	void saveCertificate(CertificateEntity certificateEntity) throws DuplicateEntryException;

	/**
	 * Gets the Tenant CA certificate
	 *
	 * @param tenantId - tenant id identifier
	 * @return
	 * @throws ResourceNotFoundException
	 * @throws DuplicateEntryException
	 */
	CertificateEntity getTenantCertificate(String tenantId) throws ResourceNotFoundException, DuplicateEntryException;

}
