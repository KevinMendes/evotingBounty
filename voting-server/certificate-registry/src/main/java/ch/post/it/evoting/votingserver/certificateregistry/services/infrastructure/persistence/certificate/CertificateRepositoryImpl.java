/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.infrastructure.persistence.certificate;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.hibernate.NonUniqueResultException;

import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;

@Stateless
public class CertificateRepositoryImpl extends BaseRepositoryImpl<CertificateEntity, Long> implements CertificateRepository {

	public static final String WHITE_SPACE = " ";

	private static final String PARAMETER_CERTIFICATE_NAME = "certificateName";

	private static final String PARAMETER_TENANT_ID = "tenantId";

	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String CONSTANT_TENANT = "Tenant";

	private static final String CONSTANT_CA = "CA";

	/**
	 * @see CertificateRepository#findByName(String)
	 */
	@Override
	public CertificateEntity findByName(final String certificateName) throws ResourceNotFoundException, DuplicateEntryException {

		TypedQuery<CertificateEntity> query = entityManager
				.createQuery("SELECT c FROM CertificateEntity c WHERE c.certificateName = :certificateName", CertificateEntity.class);

		query.setParameter(PARAMETER_CERTIFICATE_NAME, certificateName);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		} catch (NonUniqueResultException e) {
			throw new DuplicateEntryException("", e);
		}
	}

	/**
	 * @see CertificateRepository#findByTenanElectionEventAndCertificateName(String,
	 * String, String)
	 */
	@Override
	public CertificateEntity findByTenanElectionEventAndCertificateName(final String tenantId, final String electionEventId,
			final String certificateName) throws ResourceNotFoundException, DuplicateEntryException {

		TypedQuery<CertificateEntity> query = entityManager.createQuery(
				"SELECT c FROM CertificateEntity c WHERE c.certificateName = :certificateName AND c.tenantId = :tenantId AND c.electionEventId = :electionEventId",
				CertificateEntity.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_CERTIFICATE_NAME, certificateName);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		} catch (NonUniqueResultException e) {
			throw new DuplicateEntryException("", e);
		}

	}

	/**
	 * @see CertificateRepository#saveCertificate(CertificateEntity)
	 */
	@Override
	public void saveCertificate(final CertificateEntity certificateEntity) throws DuplicateEntryException {
		save(certificateEntity);
	}

	/**
	 * @see CertificateRepository#findByTenantAndCertificateName(String, String)
	 */
	@Override
	public CertificateEntity findByTenantAndCertificateName(String tenantId, String certificateName)
			throws ResourceNotFoundException, DuplicateEntryException {
		TypedQuery<CertificateEntity> query = entityManager
				.createQuery("SELECT c FROM CertificateEntity c WHERE c.certificateName = :certificateName AND c.tenantId = :tenantId",
						CertificateEntity.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_CERTIFICATE_NAME, certificateName);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		} catch (NonUniqueResultException e) {
			throw new DuplicateEntryException("", e);
		}
	}

	/**
	 * @see CertificateRepository#checkIfCertificateExist(String, String)
	 */
	@Override
	public Long checkIfCertificateExist(String tenantId, String name) {
		TypedQuery<Long> query = entityManager
				.createQuery("SELECT COUNT(*) FROM CertificateEntity c WHERE c.certificateName = :certificateName AND c.tenantId = :tenantId",
						Long.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_CERTIFICATE_NAME, name);

		return query.getResultList().get(0);

	}

	/**
	 * @see CertificateRepository#getTenantCertificate(String)
	 */
	@Override
	public CertificateEntity getTenantCertificate(String tenantId) throws ResourceNotFoundException, DuplicateEntryException {

		String certificateName = CONSTANT_TENANT.concat(WHITE_SPACE).concat(tenantId).concat(WHITE_SPACE).concat(CONSTANT_CA);
		TypedQuery<CertificateEntity> query = entityManager
				.createQuery("SELECT c FROM CertificateEntity c WHERE c.certificateName = :certificateName AND c.tenantId = :tenantId",
						CertificateEntity.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_CERTIFICATE_NAME, certificateName);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		} catch (NonUniqueResultException e) {
			throw new DuplicateEntryException("", e);
		}
	}
}
