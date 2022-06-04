/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.exists;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.input.ReversedLinesFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.securedatamanager.VotingCardSetServiceTestBase;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(VotingCardSetServiceTestSpringConfig.class)
class VotingCardSetSignServiceTest extends VotingCardSetServiceTestBase {

	private static final String ELECTION_EVENT_ID = "a3d790fd1ac543f9b0a05ca79a20c9e2";
	@Autowired
	private PathResolver pathResolver;

	@Autowired
	private VotingCardSetSignService votingCardSetSignService;

	@Autowired
	private VotingCardSetRepository votingCardSetRepositoryMock;

	@BeforeEach
	void setup() {
		Mockito.reset(votingCardSetRepositoryMock);
	}

	@Test
	void signReturnsFalseWhenTheRequestedSetIsAlreadySigned() throws IOException, GeneralCryptoLibException, ResourceNotFoundException {
		assertFalse(votingCardSetSignService.sign(ELECTION_EVENT_ID, VOTING_CARD_SET_ID, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void sign() throws IOException, GeneralCryptoLibException, ResourceNotFoundException {
		setStatusForVotingCardSetFromRepository("GENERATED", votingCardSetRepositoryMock);

		when(pathResolver.resolve(any())).thenReturn(Paths.get("src/test/resources/votingcardsetservice/"));

		assertTrue(votingCardSetSignService.sign(ELECTION_EVENT_ID, VOTING_CARD_SET_ID, SigningTestData.PRIVATE_KEY_PEM));

		final PublicKey publicKey = PemUtils.publicKeyFromPem(SigningTestData.PUBLIC_KEY_PEM);
		final Path outputPath = Paths.get("src/test/resources/votingcardsetservice/" + ELECTION_EVENT_ID + "/ONLINE/");

		verifyVoterMaterialCSVs(publicKey, outputPath);
		verifyVoteVerificationCSVs(publicKey, outputPath);
		verifyVoteVerificationJSONs(publicKey, outputPath);
		verifyExtendedAuth(publicKey, outputPath);
		verifyPrintingDataCSVs(publicKey, outputPath);
	}

	private void verifyVoteVerificationJSONs(final PublicKey publicKey, final Path outputPath) throws IOException {

		final Path signedVerificationContextData = Paths
				.get(outputPath.toString(), "voteVerification/9a0/", Constants.CONFIG_FILE_NAME_SIGNED_VERIFICATION_CONTEXT_DATA);

		assertTrue(exists(signedVerificationContextData));

		final ConfigObjectMapper mapper = new ConfigObjectMapper();

		final SignedObject signedVerificationContextDataObj = mapper.fromJSONFileToJava(signedVerificationContextData.toFile(), SignedObject.class);
		final String signatureVerCardContextData = signedVerificationContextDataObj.getSignature();

		JsonSignatureService.verify(publicKey, signatureVerCardContextData, VoteVerificationContextData.class);

		Files.deleteIfExists(signedVerificationContextData);
	}

	private void verifyExtendedAuth(final PublicKey publicKey, final Path outputPath) throws IOException, GeneralCryptoLibException {
		verifyDataCSVs(publicKey, outputPath, "extendedAuthentication", VOTING_CARD_SET_ID);
	}

	private void verifyVoteVerificationCSVs(final PublicKey publicKey, final Path outputPath) throws IOException, GeneralCryptoLibException {
		verifyDataCSVs(publicKey, outputPath, "voteVerification", "9a0");
	}

	private void verifyVoterMaterialCSVs(final PublicKey publicKey, final Path outputPath) throws IOException, GeneralCryptoLibException {
		verifyDataCSVs(publicKey, outputPath, "voterMaterial", VOTING_CARD_SET_ID);
	}

	private void verifyPrintingDataCSVs(final PublicKey publicKey, final Path outputPath) throws IOException, GeneralCryptoLibException {
		final Path printingPath = Paths.get(outputPath.toString(), "printing", VOTING_CARD_SET_ID, "/");

		final List<Path> printingFiles = Files.walk(printingPath, 1).filter(filePath -> filePath.getFileName().toString().endsWith(Constants.CSV))
				.collect(Collectors.toList());

		for (final Path csvFilePath : printingFiles) {
			if (csvFilePath.getFileName().toString().startsWith(Constants.PRINTING_DATA) && csvFilePath.getFileName().toString()
					.endsWith(Constants.CSV)) {
				final String csvAndSignature = concatenateCSVAndSignature(csvFilePath);
				final Path tempFile = Files.createTempFile("tmp", ".tmp");

				Files.write(tempFile, csvAndSignature.getBytes(StandardCharsets.UTF_8));

				assertTrue(verify(publicKey, tempFile));

				Files.deleteIfExists(Paths.get(csvFilePath + Constants.SIGN));
			}
		}
	}

	private void verifyDataCSVs(final PublicKey publicKey, final Path outputPath, final String subFolderName, final String subSubFolderName)
			throws IOException, GeneralCryptoLibException {
		final Path path = Paths.get(outputPath.toString(), subFolderName, subSubFolderName, "/");

		final List<Path> filesPath = Files.walk(path, 1).filter(filePath -> filePath.getFileName().toString().endsWith(Constants.CSV))
				.collect(Collectors.toList());

		for (final Path csvFilePath : filesPath) {
			if (csvFilePath.getFileName().toString().endsWith(Constants.CSV)) {
				final String csvAndSignature = concatenateCSVAndSignature(csvFilePath);
				final Path tempFile = Files.createTempFile("tmp", ".tmp");

				Files.write(tempFile, csvAndSignature.getBytes(StandardCharsets.UTF_8));

				assertTrue(verify(publicKey, tempFile));

				Files.deleteIfExists(Paths.get(csvFilePath + Constants.SIGN));
			}
		}
	}

	/**
	 * Verifies the signature of a given CSV file, and removes the signature from it.
	 */
	private boolean verify(final PublicKey publicKey, final Path csvSignedFile) throws IOException, GeneralCryptoLibException {

		// Validate file path
		if (csvSignedFile == null) {
			throw new IOException("Error to validate CSV file path. The given file path cannot be null.");
		} else if (!csvSignedFile.toFile().exists()) {
			throw new IOException("Error to validate CSV file path. The given file path " + csvSignedFile + ", should exist.");
		}

		// Get signature from file
		final File csvFile = csvSignedFile.toFile();
		final String signatureB64;
		try (final ReversedLinesFileReader reversedLinesFileReader = new ReversedLinesFileReader(csvFile, 4096, StandardCharsets.UTF_8)) {
			signatureB64 = reversedLinesFileReader.readLine();
		}

		// Remove signature from file
		try (final RandomAccessFile randomAccessFile = new RandomAccessFile(csvFile, "rw")) {

			final long length = randomAccessFile.length();
			final int sizeLastLine = signatureB64.getBytes(StandardCharsets.UTF_8).length + 1;
			randomAccessFile.setLength(length - sizeLastLine);
		}

		// Validate signature
		try (final FileInputStream csvFileIn = new FileInputStream(csvFile)) {

			final byte[] signatureBytes = Base64.getDecoder().decode(signatureB64);
			final AsymmetricService asymmetricService = new AsymmetricService();
			return asymmetricService.verifySignature(signatureBytes, publicKey, csvFileIn);
		}
	}

	private String concatenateCSVAndSignature(final Path filePath) throws IOException {
		final Path signedFile = Paths.get(filePath.toString() + Constants.SIGN);
		final String csvText = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
		final String signatureB64 = new String(Files.readAllBytes(signedFile), StandardCharsets.UTF_8);

		return csvText + "\n" + signatureB64;
	}
}
