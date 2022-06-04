/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationDerivedElement;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtendedAuthInformation;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedAuthenticationWriterTest {

	// this is to document what the exact format we are expecting the writer writes
	// authId, extraParam, encryptedSVK, electionEventId, salt, credentialId);
	private final int expectedFormatLength = "%s,%s,%s,%s,%s,%s".split(",").length;
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void generateOutputFilesWithCorrectDataFormat() throws Exception {

		final String tempBasePathPrefix = tempFolder.newFile().toString();
		final Path tempBasePath = Paths.get(tempBasePathPrefix + Constants.CSV);
		final int numCredentials = 8;
		final int maxNumCredsPerFile = 5;
		final int numFiles = (numCredentials + maxNumCredsPerFile - 1) / maxNumCredsPerFile;

		final String authId = "authId";
		final String extraParam = "";
		final String encryptedSVK = "encryptedSVK";
		final String electionEventId = "electionEventId";
		final String salt = "";
		final String credentialId = "credentialId";

		final ExtendedAuthenticationWriter extendedAuthenticationWriter = new ExtendedAuthenticationWriter(tempBasePath, maxNumCredsPerFile);
		extendedAuthenticationWriter.open(new ExecutionContext(Collections.emptyMap()));

		final List<GeneratedVotingCardOutput> items = new ArrayList<>();
		items.add(createOutput(authId, encryptedSVK, electionEventId, credentialId));

		// when
		for (int i = 0; i < numCredentials; i++) {
			extendedAuthenticationWriter.write(items);
		}

		// then
		final List<String> strings = new ArrayList<>();
		for (int i = 0; i < numFiles; i++) {
			final Path tempPath = Paths.get(tempBasePathPrefix + "." + i + Constants.CSV);
			final List<String> elemsFile = Files.readAllLines(tempPath);
			strings.addAll(elemsFile);
			if (i == numFiles - 1) {
				assertEquals(numCredentials - maxNumCredsPerFile, elemsFile.size());
			} else {
				assertEquals(maxNumCredsPerFile, elemsFile.size());
			}
		}

		strings.forEach((String l) -> {
			final String[] columns = l.split(",");

			assertEquals(expectedFormatLength, columns.length);
			assertEquals(authId, columns[0]);
			assertEquals(extraParam, columns[1]);
			assertEquals(encryptedSVK, columns[2]);
			assertEquals(electionEventId, columns[3]);
			assertEquals(salt, columns[4]);
			assertEquals(credentialId, columns[5]);
		});
	}

	private GeneratedVotingCardOutput createOutput(final String authId, final String encryptedSVK, final String electionEventId,
			final String credentialId) {

		final ExtendedAuthInformation extendedAuthInformation = mock(ExtendedAuthInformation.class);
		final AuthenticationDerivedElement authenticationDerivedElement = mock(AuthenticationDerivedElement.class);
		when(authenticationDerivedElement.getDerivedKeyInEx()).thenReturn(authId);
		when(extendedAuthInformation.getAuthenticationId()).thenReturn(authenticationDerivedElement);
		when(extendedAuthInformation.getExtendedAuthChallenge()).thenReturn(Optional.empty());
		when(extendedAuthInformation.getEncryptedSVK()).thenReturn(encryptedSVK);

		return GeneratedVotingCardOutput
				.success(null, null, null, null, credentialId, electionEventId, null, null, null, null, null, null, extendedAuthInformation);
	}

}
