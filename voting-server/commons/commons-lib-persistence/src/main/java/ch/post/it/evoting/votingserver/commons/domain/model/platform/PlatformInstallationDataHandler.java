/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.platform;

import java.security.cert.X509Certificate;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

public class PlatformInstallationDataHandler {

	private PlatformInstallationDataHandler() {
		super();
	}

	public static <T extends PlatformCAEntity> String savePlatformCertificateChain(final PlatformInstallationData data,
			final BaseRepository<T, Long> repository, CertificateValidationService certificateValidationService, final T platformRoot,
			final T issuerCert) {
		try {

			String issuerCertificateData = data.getPlatformRootIssuerCaPEM();
			CryptoAPIX509Certificate issuerCryptoAPIX509Certificate;

			String platformCertificateData = data.getPlatformRootCaPEM();
			X509Certificate platformCertificate = (X509Certificate) PemUtils.certificateFromPem(platformCertificateData);

			CryptoAPIX509Certificate cryptoAPIX509Certificate = new CryptoX509Certificate(platformCertificate);
			if (issuerCertificateData != null && !issuerCertificateData.isEmpty()) {
				X509Certificate issuerCertificate = (X509Certificate) PemUtils.certificateFromPem(issuerCertificateData);
				issuerCryptoAPIX509Certificate = new CryptoX509Certificate(issuerCertificate);

				X509CertificateValidationResult validateCertificateChain = validateCertificateChain(issuerCryptoAPIX509Certificate,
						cryptoAPIX509Certificate, certificateValidationService);
				if (!validateCertificateChain.isValidated()) {
					throw new IllegalStateException(
							"Certificate validation failed for the following validation types: " + validateCertificateChain.getFailedValidationTypes()
									.toString());
				}
				saveToRepository(repository, issuerCertificateData, issuerCryptoAPIX509Certificate, issuerCert);
			} else {
				X509CertificateValidationResult validationResult = validateSingleCertificate(cryptoAPIX509Certificate, certificateValidationService);
				if (!validationResult.isValidated()) {
					throw new IllegalStateException(
							"Certificate validation failed for the following validation types: " + validationResult.getFailedValidationTypes()
									.toString());
				}
			}

			saveToRepository(repository, platformCertificateData, cryptoAPIX509Certificate, platformRoot);
			return cryptoAPIX509Certificate.getSubjectDn().getOrganization();
		} catch (IllegalArgumentException | SecurityException | DuplicateEntryException | GeneralCryptoLibException | CryptographicOperationException e) {
			throw new IllegalStateException("Error while trying to initialize platformroot", e);
		}
	}

	private static <T extends PlatformCAEntity> void saveToRepository(final BaseRepository<T, Long> repository, String platformCertificateData,
			CryptoAPIX509Certificate cryptoAPIX509Certificate, T platformCaEntity) throws DuplicateEntryException {

		String platformName = cryptoAPIX509Certificate.getSubjectDn().getOrganization();
		String certificateName = cryptoAPIX509Certificate.getSubjectDn().getCommonName();

		platformCaEntity.setCertificateName(certificateName);
		platformCaEntity.setCertificateContent(platformCertificateData);
		platformCaEntity.setPlatformName(platformName);
		repository.save(platformCaEntity);
	}

	public static X509CertificateValidationResult validateSingleCertificate(CryptoAPIX509Certificate cryptoAPIX509Certificate,
			CertificateValidationService certificateValidationService) throws CryptographicOperationException {
		return certificateValidationService.validateRootCertificate(cryptoAPIX509Certificate.getCertificate());
	}

	public static X509CertificateValidationResult validateCertificateChain(CryptoAPIX509Certificate issuerCryptoAPIX509Certificate,
			CryptoAPIX509Certificate cryptoAPIX509Certificate, CertificateValidationService certificateValidationService)
			throws GeneralCryptoLibException, CryptographicOperationException {
		return certificateValidationService
				.validateIntermediateCACertificate(cryptoAPIX509Certificate.getCertificate(), issuerCryptoAPIX509Certificate.getCertificate());

	}
}
