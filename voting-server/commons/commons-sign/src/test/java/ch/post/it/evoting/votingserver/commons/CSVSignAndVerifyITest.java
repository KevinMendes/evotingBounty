/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyPair;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.votingserver.commons.sign.CSVSigner;
import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;

public class CSVSignAndVerifyITest {

	private static final Path sourceCSV = Paths.get("src/test/resources/example.csv");
	private static final Path outputCSV = Paths.get("target/example.csv");
	private static CSVSigner signer;
	private static CSVVerifier verifier;
	private static KeyPair keyPairForSigning;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@BeforeClass
	public static void setUp() throws IOException {

		signer = new CSVSigner();
		verifier = new CSVVerifier();

		AsymmetricService asymmetricService = new AsymmetricService();
		keyPairForSigning = asymmetricService.getKeyPairForSigning();
		Files.copy(sourceCSV, outputCSV, StandardCopyOption.REPLACE_EXISTING);
	}

	@Test
	public void sign_and_verify_a_given_csv_file() throws IOException, GeneralCryptoLibException {

		signer.sign(keyPairForSigning.getPrivate(), outputCSV);
		boolean verified = verifier.verify(keyPairForSigning.getPublic(), outputCSV);

		assertThat(verified, is(true));
	}

}
