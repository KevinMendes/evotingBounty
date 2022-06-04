/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import java.nio.file.Path;

import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.securedatamanager.EncryptionParametersService;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateBallotBoxesInput;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.ConfigurationEngineException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.output.BallotBoxesServiceOutput;
import ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox.BallotBoxGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.ballotbox.BallotBoxParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.exceptions.CreateBallotBoxesException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.BallotBoxParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

/**
 * This implementation generates the ballot box data.
 */
@Service
public class BallotBoxDataGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxDataGeneratorService.class);
	private static final String TAB_LOG = "\t {}";

	private final PathResolver pathResolver;
	private final BallotBoxGenerator ballotBoxGenerator;
	private final BallotBoxRepository ballotBoxRepository;
	private final ElectionEventRepository electionEventRepository;
	private final EncryptionParametersService encryptionParametersService;
	private final BallotBoxParametersHolderAdapter ballotBoxParametersHolderAdapter;

	public BallotBoxDataGeneratorService(final PathResolver pathResolver,
			final BallotBoxGenerator ballotBoxGenerator,
			final BallotBoxRepository ballotBoxRepository,
			final ElectionEventRepository electionEventRepository,
			final EncryptionParametersService encryptionParametersService,
			final BallotBoxParametersHolderAdapter ballotBoxParametersHolderAdapter) {
		this.pathResolver = pathResolver;
		this.ballotBoxGenerator = ballotBoxGenerator;
		this.ballotBoxRepository = ballotBoxRepository;
		this.electionEventRepository = electionEventRepository;
		this.encryptionParametersService = encryptionParametersService;
		this.ballotBoxParametersHolderAdapter = ballotBoxParametersHolderAdapter;
	}

	/**
	 * This method generates all the data for a ballot box.
	 *
	 * @param ballotBoxId     The identifier of the ballot box for which to generate the data.
	 * @param electionEventId The identifier of the election event to whom this ballot box belongs.
	 * @return a bean containing information about the result of the generation.
	 */
	public DataGeneratorResponse generate(final String ballotBoxId, final String electionEventId) {
		// some basic validation of the input
		final DataGeneratorResponse result = new DataGeneratorResponse();
		if (StringUtils.isEmpty(ballotBoxId)) {
			result.setSuccessful(false);
			return result;
		}

		final String ballotBoxAsJson = ballotBoxRepository.find(ballotBoxId);
		// simple check if exists data
		if (JsonConstants.EMPTY_OBJECT.equals(ballotBoxAsJson)) {
			result.setSuccessful(false);
			return result;
		}

		final JsonObject ballotBox = JsonUtils.getJsonObject(ballotBoxAsJson);
		final String ballotId = ballotBox.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);

		// The election event is retrieved in order to check later some settings
		final String electionEventAsJson = electionEventRepository.find(electionEventId);
		if (JsonConstants.EMPTY_OBJECT.equals(electionEventAsJson)) {
			result.setSuccessful(false);
			return result;
		}
		final JsonObject electionEvent = JsonUtils.getJsonObject(electionEventAsJson);

		final Path configPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR);
		final Path configElectionEventPath = configPath.resolve(electionEventId);

		final CreateBallotBoxesInput input = new CreateBallotBoxesInput();
		input.setBallotBoxID(ballotBoxId);
		input.setBallotID(ballotId);
		input.setAlias(ballotBox.getString(JsonConstants.ALIAS));
		input.setElectoralAuthorityID(ballotBox.getJsonObject(JsonConstants.ELECTORAL_AUTHORITY).getString(JsonConstants.ID));
		input.setOutputFolder(configElectionEventPath.toString());
		input.setTest(ballotBox.getString(JsonConstants.TEST));
		input.setGracePeriod(ballotBox.getString(JsonConstants.GRACE_PERIOD));

		input.setStart(ballotBox.getString(JsonConstants.DATE_FROM));
		input.setEnd(ballotBox.getString(JsonConstants.DATE_TO));
		input.setValidityPeriod(electionEvent.getJsonObject(JsonConstants.SETTINGS).getInt(JsonConstants.CERTIFICATES_VALIDITY_PERIOD));
		input.setWriteInAlphabet(electionEvent.getJsonObject(JsonConstants.SETTINGS).getString(JsonConstants.WRITE_IN_ALPHABET));

		final BallotBoxParametersHolder holder = ballotBoxParametersHolderAdapter.adapt(input);
		try {
			createBallotBoxes(holder);
		} catch (final ConfigurationEngineException e) {
			LOGGER.error(String.format("Error creating ballot box. [ballotId=%s, ballotBoxId=%s]", ballotId, ballotBoxId), e);
			result.setSuccessful(false);
		}

		return result;
	}

	@VisibleForTesting
	public BallotBoxesServiceOutput createBallotBoxes(final BallotBoxParametersHolder ballotBoxHolder) {
		try {
			LOGGER.info("Retrieve encryption parameters.");
			final GqGroup gqGroup = encryptionParametersService.load(ballotBoxHolder.getEeID());
			final EncryptionParameters encryptionParameters = new EncryptionParameters(gqGroup.getP().toString(), gqGroup.getQ().toString(),
					gqGroup.getGenerator().getValue().toString());
			ballotBoxHolder.setEncryptionParameters(encryptionParameters);

			LOGGER.info("Creating ballot boxes...");

			final BallotBoxesServiceOutput boxesServiceOutput = ballotBoxGenerator.generate(ballotBoxHolder);

			LOGGER.info("The ballot boxes were successfully created. They can be found in:");
			LOGGER.info(TAB_LOG, boxesServiceOutput.getOutputPath());

			return boxesServiceOutput;

		} catch (final Exception e) {
			throw new CreateBallotBoxesException(e);
		}
	}
}
