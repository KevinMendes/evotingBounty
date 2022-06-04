/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotBoxDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * This is an application service that manages voting card sets.
 */
@Service
public class VotingCardSetGenerateBallotService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetGenerateBallotService.class);

	private final BallotBoxRepository ballotBoxRepository;
	private final BallotDataGeneratorService ballotDataGeneratorService;
	private final BallotBoxDataGeneratorService ballotBoxDataGeneratorService;
	private final ConfigurationEntityStatusService configurationEntityStatusService;

	public VotingCardSetGenerateBallotService(
			final BallotBoxRepository ballotBoxRepository,
			final BallotDataGeneratorService ballotDataGeneratorService,
			final BallotBoxDataGeneratorService ballotBoxDataGeneratorService,
			final ConfigurationEntityStatusService configurationEntityStatusService) {
		this.ballotBoxRepository = ballotBoxRepository;
		this.ballotDataGeneratorService = ballotDataGeneratorService;
		this.ballotBoxDataGeneratorService = ballotBoxDataGeneratorService;
		this.configurationEntityStatusService = configurationEntityStatusService;
	}

	/**
	 * Generates the needed data like ballot box and ballot.
	 *
	 * @param electionEventId The id of the election event.
	 * @param votingCardSetId The id of the voting card set for which the data is generated.
	 * @return a DataGeneratorResponse containing information about the result of the generation.
	 */
	public DataGeneratorResponse generate(final String electionEventId, final String votingCardSetId) {
		DataGeneratorResponse result = new DataGeneratorResponse();

		LOGGER.info("Generating the ballot box and the ballot. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);

		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		// get ballot box from voting card set repository
		final String ballotBoxId = votingCardSetRepository.getBallotBoxId(votingCardSetId);
		if (StringUtils.isEmpty(ballotBoxId)) {
			result.setSuccessful(false);
			return result;
		}

		// get ballot from ballot box repository
		final String ballotId = ballotBoxRepository.getBallotId(ballotBoxId);
		if (StringUtils.isEmpty(ballotId)) {
			result.setSuccessful(false);
			return result;
		}

		// generate ballot data
		result = ballotDataGeneratorService.generate(ballotId, electionEventId);
		if (!result.isSuccessful()) {
			return result;
		}

		// generate ballot box data if it is not already done
		// The status locked means that it is not generated yet
		final String ballotBoxAsJson = ballotBoxRepository.find(ballotBoxId);
		final String ballotBoxStatus = JsonUtils.getJsonObject(ballotBoxAsJson).getString(JsonConstants.STATUS);
		if (Status.LOCKED.name().equals(ballotBoxStatus)) {
			result = ballotBoxDataGeneratorService.generate(ballotBoxId, electionEventId);
			if (!result.isSuccessful()) {
				return result;
			}
			configurationEntityStatusService.update(Status.READY.name(), ballotBoxId, ballotBoxRepository);
		}

		LOGGER.info("Ballot box and ballot generated successfully. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);
		return result;
	}
}
