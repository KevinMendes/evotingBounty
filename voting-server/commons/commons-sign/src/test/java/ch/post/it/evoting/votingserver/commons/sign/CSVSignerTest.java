/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.sign;

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

public class CSVSignerTest {

	private static final Path sourceCSV = Paths.get("src/test/resources/example.csv");
	private static final Path sourceCSVEmpty = Paths.get("src/test/resources/emptyExample.csv");
	private static final Path outputCSV = Paths.get("target/example.csv");
	private static final Path outputCSVEmpty = Paths.get("target/emptyExample.csv");

	private static CSVSigner csvSigner;
	private static KeyPair keyPairForSigning;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@BeforeClass
	public static void setUp() throws IOException {

		csvSigner = new CSVSigner();
		AsymmetricService asymmetricService = new AsymmetricService();
		keyPairForSigning = asymmetricService.getKeyPairForSigning();
		Files.copy(sourceCSV, outputCSV, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(sourceCSVEmpty, outputCSVEmpty, StandardCopyOption.REPLACE_EXISTING);

	}

	@Test
	public void sign_a_given_csv_file() throws IOException, GeneralCryptoLibException {

		int numberLines = Files.readAllLines(outputCSV).size();
		csvSigner.sign(keyPairForSigning.getPrivate(), outputCSV);

		assertThat(Files.readAllLines(outputCSV).size(), is(numberLines + 2));
	}

	@Test
	public void sign_a_given_empty_csv_file() throws IOException, GeneralCryptoLibException {

		int numberLines = Files.readAllLines(outputCSVEmpty).size();
		csvSigner.sign(keyPairForSigning.getPrivate(), outputCSVEmpty);

		assertThat(Files.readAllLines(outputCSVEmpty).size(), is(numberLines + 2));
	}

	@Test
	public void throw_exception_when_non_existing_file_is_passed() throws IOException, GeneralCryptoLibException {

		expectedException.expect(IOException.class);
		csvSigner.sign(keyPairForSigning.getPrivate(), Paths.get("blabla.csv"));
	}

	@Test
	public void throw_exception_when_null_file_path_is_passed() throws IOException, GeneralCryptoLibException {

		expectedException.expect(IOException.class);
		csvSigner.sign(keyPairForSigning.getPrivate(), null);
	}
}
