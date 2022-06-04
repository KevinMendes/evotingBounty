/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.service;

import java.security.cert.Certificate;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.platform.CrCertificateValidationService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateTools;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;

@Stateless
public class CertificateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CertificateService.class);
	@Inject
	CertificateRepository certificateRepository;
	@Inject
	@CrCertificateValidationService
	private CertificateValidationService certificateValidationService;

	/**
	 * Saves an intermediate or root CA into the repository
	 *
	 * @param certificateEntity
	 * @return
	 * @throws GeneralCryptoLibException
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	public X509CertificateValidationResult saveCertificateInDB(final CertificateEntity certificateEntity)
			throws GeneralCryptoLibException, DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		LOGGER.info("CR - Saving certificate to DB");

		Certificate certificate = PemUtils.certificateFromPem(certificateEntity.getCertificateContent());

		CryptoAPIX509Certificate cryptoAPIX509Certificate = CertificateTools.getCryptoX509Certificate(certificate);

		final X509DistinguishedName subjectDN = cryptoAPIX509Certificate.getSubjectDn();
		certificateEntity.setPlatformName(subjectDN.getOrganization());
		certificateEntity.setCertificateName(subjectDN.getCommonName());

		String issuerCommonName = cryptoAPIX509Certificate.getIssuerDn().getCommonName();
		String subjectCommonName = cryptoAPIX509Certificate.getSubjectDn().getCommonName();
		X509CertificateValidationResult validationResult;

		if (!issuerCommonName.equals(subjectCommonName)) {

			LOGGER.info("CR - issuer and subject common names are equal");

			CertificateEntity parentEntity = certificateRepository.findByName(issuerCommonName);

			Certificate parentCertificate = PemUtils.certificateFromPem(parentEntity.getCertificateContent());

			validationResult = certificateValidationService.validateIntermediateCACertificate(certificate, parentCertificate);

		} else {

			LOGGER.info("CR - issuer and subject common names are not equal");

			validationResult = certificateValidationService.validateRootCertificate(certificate);
		}
		if (validationResult.isValidated()) {

			LOGGER.info("CR - validation has passed - will save in repository");

			certificateRepository.save(certificateEntity);
			LOGGER.info("CR - certificate saved.");
		}
		return validationResult;
	}

}
