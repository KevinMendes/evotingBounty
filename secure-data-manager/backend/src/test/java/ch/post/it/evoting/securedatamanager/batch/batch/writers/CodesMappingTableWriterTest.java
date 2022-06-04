/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.writers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.batch.item.ExecutionContext;

import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.beans.VerificationCardCodesDataPack;

public class CodesMappingTableWriterTest {

	private final ConfigObjectMapper objectMapper = new ConfigObjectMapper();
	// this is to document what the exact format we are expecting the writer writes
	private final int expectedFormatLength = "%s,%s".split(",").length;

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

		final CodesMappingTableWriter codesMappingTableWriter = new CodesMappingTableWriter(tempBasePath, maxNumCredsPerFile);
		codesMappingTableWriter.open(new ExecutionContext(Collections.emptyMap()));

		final String verificationCardId = "verificationCardId";
		final List<GeneratedVotingCardOutput> items = new ArrayList<>();
		items.add(createOutput(verificationCardId));

		// when
		for (int i = 0; i < numCredentials; i++) {
			codesMappingTableWriter.write(items);
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
			assertEquals(verificationCardId, columns[0]);

			final String json = new String(Base64.getDecoder().decode(columns[1]), StandardCharsets.UTF_8);
			try {
				assertTrue(objectMapper.fromJSONToJava(json, Map.class).isEmpty());
			} catch (final IOException e) {
				fail("unexpected format");
			}
		});
	}

	private GeneratedVotingCardOutput createOutput(final String verificationCardId) {
		final String ballotCastingKey = "ignored";
		final String voteCastingCode = "ignored";

		final VerificationCardCodesDataPack verificationCardCodesPack = new VerificationCardCodesDataPack(Collections.emptyMap(), ballotCastingKey,
				voteCastingCode, Collections.emptyMap());

		return GeneratedVotingCardOutput
				.success(null, null, null, null, null, null, verificationCardId, null, null, null, null, verificationCardCodesPack, null);
	}

}
