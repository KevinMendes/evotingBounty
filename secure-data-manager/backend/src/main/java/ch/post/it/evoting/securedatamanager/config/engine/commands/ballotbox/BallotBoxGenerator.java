/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox;

import static ch.post.it.evoting.securedatamanager.config.commons.config.logevents.ConfigGeneratorLogEvents.GENBB_SUCCESS_CREATED_AND_STORED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.domain.election.BallotBox;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.output.BallotBoxesServiceOutput;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.CreateBallotBoxesException;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.GenerateBallotBoxesException;

/**
 * Generates a given number of {@link BallotBox}, which are linked to the provided {@link Ballot}.
 */
@Service
public class BallotBoxGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxGenerator.class);

	private final ConfigObjectMapper mapper;

	public BallotBoxGenerator() {
		this.mapper = new ConfigObjectMapper();
	}

	/**
	 * Generates as many {@link BallotBox} as ballot box IDs passed as parameter.
	 */
	public BallotBoxesServiceOutput generate(final BallotBoxParametersHolder holder) {

		if (!doesBallotIdMatchAnyExistingBallot(holder.getBallotID(),
				Paths.get(holder.getOutputPath().toString(), Constants.CONFIG_DIR_NAME_ONLINE, Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION,
						Constants.CONFIG_DIR_NAME_BALLOTS, holder.getBallotID()).toAbsolutePath())) {
			throw new GenerateBallotBoxesException("The specified Ballot ID does not match any existing ballot.");
		}

		final Path absoluteOnlinePath = Paths.get(holder.getOutputPath().toString(), Constants.CONFIG_DIR_NAME_ONLINE,
				Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION, Constants.CONFIG_DIR_NAME_BALLOTS, holder.getBallotID(),
				Constants.CONFIG_DIR_NAME_BALLOTBOXES).toAbsolutePath();

		LOGGER.info("Generating ballot box... [ballotBoxId: {}]", holder.getBallotBoxID());
		final Path onlinePath = Paths.get(absoluteOnlinePath.toString(), holder.getBallotBoxID());
		createDirectories(onlinePath);

		try {
			storeOnlineBallotBox(holder, onlinePath);
		} catch (final IOException e) {
			throw new CreateBallotBoxesException("An error occurred while saving info on online ballot box id folder: " + e.getMessage(), e);
		}

		LOGGER.info("{} [electionEventId: {}, ballotBoxId: {}]", GENBB_SUCCESS_CREATED_AND_STORED.getInfo(), holder.getEeID(),
				holder.getBallotBoxID());

		final BallotBoxesServiceOutput ballotBoxesServiceOutput = new BallotBoxesServiceOutput();
		ballotBoxesServiceOutput.setOutputPath(absoluteOnlinePath.toString());

		return ballotBoxesServiceOutput;
	}

	private boolean doesBallotIdMatchAnyExistingBallot(final String ballotID, final Path ballotsDirectoryPath) {

		final Ballot ballotFromFile;
		final Path enrichedBallotPath = Paths.get(ballotsDirectoryPath.toString(), Constants.CONFIG_FILE_NAME_BALLOT_JSON);

		try {
			ballotFromFile = mapper.fromJSONFileToJava(enrichedBallotPath.toFile(), Ballot.class);

			if (ballotFromFile.getId().equals(ballotID)) {
				return true;
			}
		} catch (final IOException e) {
			throw new CreateBallotBoxesException("An error reconstructing a ballot from a JSON file: " + e.getMessage(), e);
		}

		return false;
	}

	private void storeOnlineBallotBox(final BallotBoxParametersHolder holder, final Path onlinePath) throws IOException {

		final BallotBox ballotBox = new BallotBox();

		ballotBox.setBid(holder.getBallotID());
		ballotBox.setEeid(holder.getEeID());
		ballotBox.setId(holder.getBallotBoxID());
		ballotBox.setAlias(holder.getAlias());
		ballotBox.setStartDate(holder.getInputDataPack().getStartDate().toString());
		ballotBox.setEndDate(holder.getInputDataPack().getEndDate().toString());
		ballotBox.setElectoralAuthorityId(holder.getElectoralAuthorityID());
		ballotBox.setTest(Boolean.parseBoolean(holder.getTest()));
		ballotBox.setGracePeriod(holder.getGracePeriod());
		ballotBox.setWriteInAlphabet(holder.getWriteInAlphabet());
		ballotBox.setEncryptionParameters(holder.getEncryptionParameters());

		final Path pathFile = Paths.get(onlinePath.toString(), Constants.CONFIG_DIR_NAME_BALLOTBOX_JSON);
		mapper.fromJavaToJSONFileWithoutNull(ballotBox, pathFile.toFile());
	}

	private void createDirectories(final Path onlinePath) {
		try {
			Files.createDirectories(onlinePath);
		} catch (final IOException e) {
			throw new IllegalArgumentException("An error occurred while creating the following path: " + onlinePath, e);
		}
	}
}