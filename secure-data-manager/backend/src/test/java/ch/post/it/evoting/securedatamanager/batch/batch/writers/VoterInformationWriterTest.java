/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.commons.Constants;

public class VoterInformationWriterTest {

	private final String votingCardId = "votingCardId";
	private final String ballotId = "ballotId";
	private final String ballotBoxId = "ballotBoxId";
	private final String credentialId = "credentialId";
	private final String electionEventId = "electionEventId";
	private final String votingCardSetId = "votingCardSetId";
	private final String verificationCardId = "verificationCardId";
	private final String verificationCardSetId = "verificationCardSetId";
	// this is to document what the exact format we are expecting the writer writes
	// votingCardId, ballotId, ballotBoxId, credentialId,electionEventId, votingCardSetId, verificationCardId, verificationCardSetId
	private final int expectedFormatLength = "%s,%s,%s,%s,%s,%s,%s,%s".split(",").length;
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void generateOutputFilesWithCorrectDataFormat() throws Exception {
		// given
		final String tempBasePathPrefix = tempFolder.newFile().toString();
		final Path tempBasePath = Paths.get(tempBasePathPrefix + Constants.CSV);
		final int numCredentials = 8;
		final int maxNumCredsPerFile = 5;
		final int numFiles = (numCredentials + maxNumCredsPerFile - 1) / maxNumCredsPerFile;

		final VoterInformationWriter voterInformationWriter = new VoterInformationWriter(tempBasePath, maxNumCredsPerFile);
		voterInformationWriter.open(new ExecutionContext(Collections.emptyMap()));

		final List<GeneratedVotingCardOutput> items = new ArrayList<>();
		items.add(createOutput());

		// when
		for (int i = 0; i < numCredentials; i++) {
			voterInformationWriter.write(items);
		}

		// then
		final List<String> strings = new ArrayList<>();
		for (int i = 0; i < numFiles; i++) {
			final Path tempPath = Paths.get(tempBasePathPrefix + "." + i + Constants.CSV);
			final List<String> elemsFile = Files.readAllLines(tempPath);
			strings.addAll(elemsFile);
			if (i == numFiles - 1) {
				assertEquals(numCredentials - (numFiles - 1) * maxNumCredsPerFile, elemsFile.size());
			} else {
				assertEquals(maxNumCredsPerFile, elemsFile.size());
			}
		}

		strings.forEach((String l) -> {
			final String[] columns = l.split(",");
			assertEquals(expectedFormatLength, columns.length);
			assertEquals(votingCardId, columns[0]);
			assertEquals(ballotId, columns[1]);
			assertEquals(ballotBoxId, columns[2]);
			assertEquals(credentialId, columns[3]);
			assertEquals(electionEventId, columns[4]);
			assertEquals(votingCardSetId, columns[5]);
			assertEquals(verificationCardId, columns[6]);
			assertEquals(verificationCardSetId, columns[7]);
		});
	}

	private GeneratedVotingCardOutput createOutput() {
		return GeneratedVotingCardOutput
				.success(votingCardId, votingCardSetId, ballotId, ballotBoxId, credentialId, electionEventId, verificationCardId,
						verificationCardSetId, null, null, null, null, null);
	}

}
