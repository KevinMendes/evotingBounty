/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VotingCardCredentialDataPack;

@RunWith(MockitoJUnitRunner.class)
public class CredentialDataWriterTest {

	private static final int DEFAULT_MAX_NUM_CREDENTIALS_PER_FILE = 50000;

	private static final String KEY_STORE_JSON = "Key store JSON";

	private static final String CREDENTIAL_ID = "credentialId";

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private String tempBasePathPrefix;

	private Path tempBasePath;

	private Path tempFolderPath;

	@Before
	public void setup() throws IOException {
		tempBasePathPrefix = tempFolder.newFile().toString();
		tempBasePath = Paths.get(tempBasePathPrefix + Constants.CSV);
		tempFolderPath = tempBasePath.getParent();
	}

	@Test
	public void generateOutputFileWithCorrectDataFormat() throws Exception {

		final int numCredentials = 3;

		final CredentialDataWriter credentialDataWriter = new CredentialDataWriter(tempBasePath, DEFAULT_MAX_NUM_CREDENTIALS_PER_FILE);
		credentialDataWriter.open(new ExecutionContext(Collections.emptyMap()));

		final List<GeneratedVotingCardOutput> items = new ArrayList<>();
		for (int i = 0; i < numCredentials; i++) {
			items.add(createOutput());
		}

		credentialDataWriter.write(items);

		final List<String> lineStrings = Files.readAllLines(Paths.get(tempBasePathPrefix + ".0" + Constants.CSV));
		assertEquals(numCredentials, lineStrings.size());
		assertFileContent(lineStrings);
	}

	@Test
	public void generateMultipleOutputFilesWithCorrectDataFormat() throws Exception {

		final int numCredentials = 10;
		final int maxNumCredentialsPerFile = 3;
		final int numCredentialsRemaining = numCredentials % maxNumCredentialsPerFile;

		final CredentialDataWriter credentialDataWriter = new CredentialDataWriter(tempBasePath, maxNumCredentialsPerFile);
		credentialDataWriter.open(new ExecutionContext(Collections.emptyMap()));

		final List<GeneratedVotingCardOutput> items = new ArrayList<>();
		items.add(createOutput());
		for (int i = 0; i < numCredentials; i++) {
			credentialDataWriter.write(items);
		}

		for (int i = 0; i < (maxNumCredentialsPerFile - 1); i++) {
			final List<String> lineStrings = Files.readAllLines(Paths.get(tempBasePathPrefix + "." + i + Constants.CSV));
			assertEquals(maxNumCredentialsPerFile, lineStrings.size());
			assertFileContent(lineStrings);
		}

		final List<String> lineStrings = Files.readAllLines(Paths.get(tempBasePathPrefix + "." + maxNumCredentialsPerFile + Constants.CSV));
		assertEquals(numCredentialsRemaining, lineStrings.size());
		assertFileContent(lineStrings);
	}

	@Test
	public void deletePreExistingOutputFiles() throws Exception {

		final int numCredentialDataFiles = 10;
		final int numOtherFiles = 7;

		for (int i = 0; i < numCredentialDataFiles; i++) {
			File.createTempFile(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA, Constants.CSV, tempFolderPath.toFile());
		}
		for (int i = 0; i < numOtherFiles; i++) {
			File.createTempFile(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION, Constants.CSV, tempFolderPath.toFile());
		}

		assertEquals(numCredentialDataFiles, countExistingFiles(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA));
		assertEquals(numOtherFiles, countExistingFiles(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION));

		new CredentialDataWriter(tempBasePath, DEFAULT_MAX_NUM_CREDENTIALS_PER_FILE);

		assertEquals(0, countExistingFiles(Constants.CONFIG_FILE_NAME_CREDENTIAL_DATA));
		assertEquals(numOtherFiles, countExistingFiles(Constants.CONFIG_FILE_NAME_VOTER_INFORMATION));
	}

	private GeneratedVotingCardOutput createOutput() {

		final VotingCardCredentialDataPack votingCardCredentialDataPack = mock(VotingCardCredentialDataPack.class);
		when(votingCardCredentialDataPack.getSerializedKeyStore()).thenReturn(KEY_STORE_JSON);

		return GeneratedVotingCardOutput
				.success(null, null, null, null, CREDENTIAL_ID, null, null, null, null, votingCardCredentialDataPack, null, null, null);
	}

	private void assertFileContent(final List<String> lineStrings) {

		final int expectedFormatLength = "%s,%s".split(",").length;

		lineStrings.forEach((String l) -> {
			final String[] columns = l.split(",");

			assertEquals(expectedFormatLength, columns.length);
			assertEquals(CREDENTIAL_ID, columns[0]);

			final String decodedKeyStore = columns[1];
			assertEquals(KEY_STORE_JSON, decodedKeyStore);
		});
	}

	private int countExistingFiles(final String prefix) throws IOException {

		final List<File> filesFound = Files.walk(tempFolderPath).map(Path::toFile)
				.filter(file -> (file.getName().startsWith(prefix) && file.getName().endsWith(Constants.CSV))).collect(Collectors.toList());

		return filesFound.size();
	}
}
