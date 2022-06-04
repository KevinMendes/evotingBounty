/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignerDigestMismatchException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;

import io.jsonwebtoken.SignatureException;

@ExtendWith(MockitoExtension.class)
class SignaturesVerifierServiceTest {

	private static final String ELECTION_EVENT_ID = "id";

	private static Path primesFilePath;
	private static Path primesSignatureFilePath;
	private static Path primesSignatureInvalid;
	private static Path invalidPrimesFilePath;
	private static Path integrationCAFilePath;
	private static Path integrationCAInvalidFilePath;
	private static Path invalidIntegrationCAFilePath;
	private static Path encryptionParametersJwtPath;
	private static Path trustedChainInvalidPath;
	private static Path invalidEncryptionParametersJwtPath;
	private static Path encryptionParametersJwtInvalidPath;

	@Spy
	private static SignatureVerifier signatureVerifier;

	@Spy
	@InjectMocks
	private SignaturesVerifierService signaturesVerifierService;

	@Mock
	private PathResolver pathResolver;

	@Mock
	private Path mockPath;

	@BeforeAll
	static void init() throws CertificateException, NoSuchProviderException {
		signatureVerifier = new SignatureVerifier();

		final Path customerOutputPath = Paths.get("src", "test", "resources");

		integrationCAFilePath = customerOutputPath.resolve("integrationCA.pem");
		integrationCAInvalidFilePath = customerOutputPath.resolve("integrationCA_invalid.pem");
		invalidIntegrationCAFilePath = customerOutputPath.resolve("integration_doesnt_exist.pem");

		primesFilePath = customerOutputPath.resolve("primes.csv");
		primesSignatureFilePath = customerOutputPath.resolve("primes.csv.p7");
		primesSignatureInvalid = customerOutputPath.resolve("primes_invalid.csv.p7");
		invalidPrimesFilePath = customerOutputPath.resolve("primes_doesnt_exist.csv");

		encryptionParametersJwtPath = customerOutputPath.resolve("encryptionParameters.json.sign");
		encryptionParametersJwtInvalidPath = customerOutputPath.resolve("encryptionParameters_invalid.json.sign");
		invalidEncryptionParametersJwtPath = customerOutputPath.resolve("encryptionParameters_doesnt_exist.json.sign");
		trustedChainInvalidPath = customerOutputPath.resolve("trustedChain_invalid.pem");

	}

	@Test
	void verifyPrimesWrongSignature() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM)).thenReturn(integrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureInvalid);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		assertThrows(CMSSignerDigestMismatchException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyPrimesWrongTrustedCA() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM))
				.thenReturn(integrationCAInvalidFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		assertThrows(GeneralCryptoLibException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyPrimesFileNonExistent() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM))
				.thenReturn(integrationCAInvalidFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(invalidPrimesFilePath);

		assertThrows(NoSuchFileException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyPrimesTrustedCANonExistent() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM))
				.thenReturn(invalidIntegrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(invalidPrimesFilePath);

		assertThrows(FileNotFoundException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyPrimesSignatureFileNonExistent() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM))
				.thenReturn(integrationCAInvalidFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(invalidPrimesFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		assertThrows(NoSuchFileException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyJwt() throws IOException, CertificateException, CMSException, GeneralCryptoLibException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM)).thenReturn(integrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT,
						Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON)).thenReturn(encryptionParametersJwtPath);

		final VerifiableElGamalEncryptionParameters params = signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID);

		assertEquals("3", params.getGroup().getG().toString());
	}

	@Test
	void verifyJwtWrongTrustedChain() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM), trustedChainInvalidPath);

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM)).thenReturn(integrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT,
						Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON)).thenReturn(encryptionParametersJwtPath);

		assertThrows(SignatureException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyJwtNonExistent() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM)).thenReturn(integrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT,
						Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON)).thenReturn(invalidEncryptionParametersJwtPath);

		assertThrows(FileNotFoundException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}

	@Test
	void verifyJwtInvalidSignature() throws IOException {

		final Path tmpPath = Files.createTempDirectory("dir");
		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM))
				.thenReturn(tmpPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM));

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT))
				.thenReturn(mockPath);

		when(pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM)).thenReturn(integrationCAFilePath);

		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN)).thenReturn(primesSignatureFilePath);
		when(mockPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV)).thenReturn(primesFilePath);

		when(pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, ELECTION_EVENT_ID, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT,
						Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON)).thenReturn(encryptionParametersJwtInvalidPath);

		assertThrows(SignatureException.class, () -> signaturesVerifierService.verifyEncryptionParams(ELECTION_EVENT_ID));
	}
}
