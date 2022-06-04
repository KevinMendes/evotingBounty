/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.platform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.util.Calendar;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationServiceImpl;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

class PlatformInstallationDataHandlerTest {

	// Common Name: Root Org CA
	// Organization: Root Org
	// Organization Unit: Online Voting
	// Locality:
	// Country: CH
	// Valid From: Calendar.getInstance()
	// Valid To: Calendar.getInstance() + 1y
	// Issuer: Root Org CA, Root Org
	private static String PLATFORM_CA;

	// Common Name: swisspost Root CA
	// Organization: SwissPost
	// Organization Unit: Online Voting
	// Locality:
	// Country: CH
	// Valid From: Calendar.getInstance()
	// Valid To: Calendar.getInstance() + 2y
	// Issuer: TEST INTERMEDIATE CA, TEST ORGANIZATION (intermediate CA)
	private static String PLATFORM_CA_RECERTIFIED;

	// Common Name: TEST INTERMEDIATE CA
	// Organization: TEST ORGANIZATION
	// Organization Unit: TEST ORGANIZATIONAL UNIT
	// Country: CH
	// Valid From: Calendar.getInstance()
	// Valid To: Calendar.getInstance() + 3y
	// Issuer: TEST ROOT CA, TEST ORGANIZATION (root CA)
	private static String INTERMEDIATE_CA;

	private final CertificateValidationService certificateValidationService = new CertificateValidationServiceImpl();

	@SuppressWarnings("unchecked")
	protected BaseRepository<PlatformCAEntity, Long> repository = Mockito.mock(BaseRepository.class);

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {
		createCertificates();
	}

	private static void createCertificates() throws GeneralCryptoLibException {
		final AsymmetricServiceAPI asymmetricService = new AsymmetricService();
		final Calendar platformNow = Calendar.getInstance();
		final Calendar platformFuture = Calendar.getInstance();
		platformFuture.add(Calendar.YEAR, 1);
		final KeyPair platformKeyPair = asymmetricService.getKeyPairForSigning();
		final CryptoAPIX509Certificate createRootSelfSignedCertificate = CertificateUtil
				.createRootSelfSignedCertificate("Root Org CA", "Root Org", "Online Voting", "CH", platformNow.getTime(), platformFuture.getTime(),
						platformKeyPair);
		PLATFORM_CA = PemUtils.certificateToPem(createRootSelfSignedCertificate.getCertificate());

		final Calendar maintTrustedNow = Calendar.getInstance();
		final Calendar maintTrustedFuture = Calendar.getInstance();
		maintTrustedFuture.add(Calendar.YEAR, 3);
		final KeyPair maintTrustedAuthKeyPair = asymmetricService.getKeyPairForSigning();
		final CryptoAPIX509Certificate maintTrustedAuthSelfSignedCertificate = CertificateUtil
				.createRootSelfSignedCertificate("TEST ROOT CA", "TEST ORGANIZATION", "TEST ORGANIZATIONAL UNIT", "CH", maintTrustedNow.getTime(),
						maintTrustedFuture.getTime(), maintTrustedAuthKeyPair);

		final Calendar intermediateNow = Calendar.getInstance();
		final Calendar intermediateFuture = Calendar.getInstance();
		intermediateFuture.add(Calendar.YEAR, 2);
		final KeyPair intermediateKeyPair = asymmetricService.getKeyPairForSigning();
		final CryptoAPIX509Certificate intermediateCert = CertificateUtil
				.createIntermediateCertificate("TEST INTERMEDIATE CA", "TEST ORGANIZATION", "TEST ORGANIZATIONAL UNIT", "CH",
						intermediateNow.getTime(), intermediateFuture.getTime(), intermediateKeyPair, maintTrustedAuthKeyPair,
						maintTrustedAuthSelfSignedCertificate.getSubjectDn());
		INTERMEDIATE_CA = PemUtils.certificateToPem(intermediateCert.getCertificate());

		final Calendar platformRecertifiedNow = Calendar.getInstance();
		final Calendar platformRecertifiedFuture = Calendar.getInstance();
		platformRecertifiedFuture.add(Calendar.YEAR, 1);
		final KeyPair platformRecertifiedKeyPair = asymmetricService.getKeyPairForSigning();
		final CryptoAPIX509Certificate platformRecertifiedCert = CertificateUtil
				.createIntermediateCertificate("swisspost Root CA", "SwissPost", "Online Voting", "CH", platformRecertifiedNow.getTime(),
						platformRecertifiedFuture.getTime(), platformRecertifiedKeyPair, intermediateKeyPair, intermediateCert.getSubjectDn());
		PLATFORM_CA_RECERTIFIED = PemUtils.certificateToPem(platformRecertifiedCert.getCertificate());
	}

	@Test
	void testPlatformCertificateChain() {
		final PlatformInstallationData data = new PlatformInstallationData();
		data.setPlatformRootCaPEM(PLATFORM_CA_RECERTIFIED);
		data.setPlatformRootIssuerCaPEM(INTERMEDIATE_CA);

		assertDoesNotThrow(() -> PlatformInstallationDataHandler.savePlatformCertificateChain(data, repository, certificateValidationService,
				new PlatformCAEntity(), new PlatformCAEntity()));
	}

	@Test
	void testInvalidPlatformCertificateChain() {
		final PlatformInstallationData data = new PlatformInstallationData();
		data.setPlatformRootCaPEM(INTERMEDIATE_CA);
		data.setPlatformRootIssuerCaPEM(PLATFORM_CA);

		final PlatformCAEntity platformRoot = new PlatformCAEntity();
		final PlatformCAEntity issuerCert = new PlatformCAEntity();

		final IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> PlatformInstallationDataHandler.savePlatformCertificateChain(data, repository, certificateValidationService, platformRoot,
						issuerCert));
		assertEquals("Certificate validation failed for the following validation types: [SIGNATURE]", exception.getMessage());

	}

	@Test
	void testSelfSignedPlatformCertificate() {
		final PlatformInstallationData data = new PlatformInstallationData();
		data.setPlatformRootCaPEM(PLATFORM_CA);

		assertDoesNotThrow(() -> PlatformInstallationDataHandler.savePlatformCertificateChain(data, repository, certificateValidationService,
				new PlatformCAEntity(), new PlatformCAEntity()));
	}

	@Test
	void testInvalidSelfSignedPlatformCertificate() {
		final PlatformInstallationData data = new PlatformInstallationData();
		data.setPlatformRootCaPEM(PLATFORM_CA_RECERTIFIED);

		final PlatformCAEntity platformRoot = new PlatformCAEntity();
		final PlatformCAEntity issuerCert = new PlatformCAEntity();

		final IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> PlatformInstallationDataHandler.savePlatformCertificateChain(data, repository, certificateValidationService, platformRoot,
						issuerCert));
		assertEquals("Certificate validation failed for the following validation types: [SIGNATURE]", exception.getMessage());
	}

}
