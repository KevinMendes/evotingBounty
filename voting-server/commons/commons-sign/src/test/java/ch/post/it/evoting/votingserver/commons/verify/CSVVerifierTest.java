/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;
import java.security.PublicKey;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.votingserver.commons.sign.CSVSigner;

public class CSVVerifierTest {

	private static final Path sourceCSVNotSigned = Paths.get("src/test/resources/example.csv");
	private static final Path sourceCSVWithWrongSignature = Paths.get("src/test/resources/signedExampleWrongSignature.csv");
	private static final Path sourceCSVWithModifiedContent = Paths.get("src/test/resources/signedExampleModifiedContent.csv");
	private static final Path outputCSV = Paths.get("target/signedExample.csv");
	private static final Path outputCSVNotSigned = Paths.get("target/exampleNotSigned.csv");
	private static final Path outputCSVWithWrongSignature = Paths.get("target/signedExampleWrongSignature.csv");
	private static final Path outputCSVWithModifiedContent = Paths.get("target/signedExampleModifiedContent.csv");
	private static CSVVerifier csvVerifier;
	private static PublicKey publicKey;

	@BeforeClass
	public static void setUp() throws GeneralCryptoLibException, IOException {

		csvVerifier = new CSVVerifier();
		String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvhIIHMFn8y4gyR3Gb0H5\n"
				+ "a8ZJzgawlzqttp6Zq8Ri2lBSdK6M0YrRc9W8HH2EwEOYTNW7Y5Jmvn0a2PRipCtu\n"
				+ "R1UEqTeSUHRBSXcxPm4oyHVnChQbshvpHYfNJA6xzC04S1PibrwC1TnIyMb/w30X\n"
				+ "zQmwb1FmRlanZz5omq9NpDK7Lg3kLAZQogqTvhmwhTPZxElFYJ38PutZEDWj+OPL\n"
				+ "XMcLo2WiCnW/uLsMca0uxUci/q/axRIrzmdb2FSkrKHTgrE9LKiU/SdYFaRyzEce\n"
				+ "je/Sd5MMiBGoQK2iy8j/rHKmktbRcWyp8KhqbQ7EZttsIwFnyuqpIA1x0aRAUOj6\n" + "mQIDAQAB\n" + "-----END PUBLIC KEY-----";

		publicKey = PemUtils.publicKeyFromPem(publicKeyPEM);

		Files.copy(sourceCSVNotSigned, outputCSV, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceCSVNotSigned, outputCSVNotSigned, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceCSVWithWrongSignature, outputCSVWithWrongSignature, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceCSVWithModifiedContent, outputCSVWithModifiedContent, StandardCopyOption.REPLACE_EXISTING);
	}

	@Test
	public void verify_a_given_signed_csv_file() throws IOException, GeneralCryptoLibException {
		CSVSigner signer = new CSVSigner();
		AsymmetricService asymmetricService = new AsymmetricService();
		KeyPair keyPairForSigning = asymmetricService.getKeyPairForSigning();
		int originalNumberOfLines = Files.readAllLines(outputCSV).size();
		signer.sign(keyPairForSigning.getPrivate(), outputCSV);

		int numberLines = Files.readAllLines(outputCSV).size();
		boolean verified = csvVerifier.verify(keyPairForSigning.getPublic(), outputCSV);

		assertEquals(numberLines - 2, Files.readAllLines(outputCSV).size());
		assertEquals(numberLines - 2, originalNumberOfLines);
		assertTrue(verified);
	}

	@Test
	public void not_verify_a_given_signed_csv_file_with_a_modified_signature() throws IOException, GeneralCryptoLibException {

		boolean verified = csvVerifier.verify(publicKey, outputCSVWithWrongSignature);

		assertFalse(verified);
	}

	@Test
	public void not_verify_a_given_signed_csv_file_with_a_modified_content() throws IOException, GeneralCryptoLibException {

		boolean verified = csvVerifier.verify(publicKey, outputCSVWithModifiedContent);

		assertFalse(verified);
	}

	@Test
	public void throw_exception_when_non_existing_file_is_passed() {
		assertThrows(IOException.class, () -> csvVerifier.verify(publicKey, Paths.get("blabla.csv")));
	}

	@Test
	public void throw_exception_when_null_file_path_is_passed() {
		assertThrows(IOException.class, () -> csvVerifier.verify(publicKey, null));
	}

}
