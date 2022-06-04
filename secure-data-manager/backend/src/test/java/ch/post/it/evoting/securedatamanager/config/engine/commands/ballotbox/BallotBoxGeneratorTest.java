/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans.ElectionInputDataPack;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(BallotBoxGeneratorTestSpringConfig.class)
class BallotBoxGeneratorTest {

	private final static String BALLOT_ID = "a5c0305db01142e786533cb48df1c794";
	private final static String BALLOT_BOX_ID = "ballotBoxId";
	private final static String BALLOT_FILENAME = "ballot.json";
	private final static String OUTPUT_PATH_TEMP_DIR_NAME = "outputPath";

	private static Path outputPath;

	@Autowired
	private BallotBoxGenerator ballotBoxGenerator;

	@Spy
	@InjectMocks
	private BallotBoxParametersHolder ballotBoxParametersHolder;

	@Spy
	private ElectionInputDataPack electionInputDataPack;

	@BeforeAll
	static void setUp() throws IOException, URISyntaxException {

		outputPath = Files.createTempDirectory(OUTPUT_PATH_TEMP_DIR_NAME);

		final Path absoluteOnlinePath = Paths.get(outputPath.toString(), Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION,
				Constants.CONFIG_DIR_NAME_BALLOTS, BALLOT_ID, Constants.CONFIG_DIR_NAME_BALLOTBOXES).toAbsolutePath();

		final Path absoluteOfflinePath = Paths.get(outputPath.toString(), Constants.CONFIG_DIR_NAME_OFFLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION,
				Constants.CONFIG_DIR_NAME_BALLOTS, BALLOT_ID, Constants.CONFIG_DIR_NAME_BALLOTBOXES).toAbsolutePath();

		Files.createDirectories(Paths.get(absoluteOnlinePath.toString(), BALLOT_BOX_ID));
		Files.createDirectories(Paths.get(absoluteOfflinePath.toString(), BALLOT_BOX_ID));

		final Path ballotFilePath = Paths.get(outputPath.toString(), Constants.CONFIG_DIR_NAME_ONLINE, Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION,
				Constants.CONFIG_DIR_NAME_BALLOTS, BALLOT_ID, BALLOT_FILENAME);

		// Copy file from test resources to output path
		Files.copy(Paths.get(BallotBoxGeneratorTest.class.getResource("/" + BALLOT_FILENAME).toURI()), ballotFilePath,
				StandardCopyOption.REPLACE_EXISTING);
	}

	@AfterAll
	static void tearDown() throws IOException {
		FileUtils.deleteDirectory(outputPath.toFile());
	}

	@Test
	void generateValidBallotBox() {

		final ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		electionInputDataPack.setStartDate(now);
		electionInputDataPack.setEndDate(now.plusYears(1));

		when(ballotBoxParametersHolder.getBallotBoxID()).thenReturn(BALLOT_BOX_ID);
		when(ballotBoxParametersHolder.getBallotID()).thenReturn(BALLOT_ID);
		when(ballotBoxParametersHolder.getOutputPath()).thenReturn(outputPath);
		when(ballotBoxParametersHolder.getInputDataPack()).thenReturn(electionInputDataPack);

		assertDoesNotThrow(() -> ballotBoxGenerator.generate(ballotBoxParametersHolder));
	}
}
