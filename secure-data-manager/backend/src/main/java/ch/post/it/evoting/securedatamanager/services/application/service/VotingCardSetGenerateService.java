/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.configuration.setupvoting.ReturnCodesMappingTableFileCreationService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.service.VotingCardSetDataGeneratorService;

/**
 * This is an application service that manages voting card sets.
 */
@Service
public class VotingCardSetGenerateService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetGenerateService.class);

	private final IdleStatusService idleStatusService;
	private final VotingCardSetDataGeneratorService votingCardSetDataGeneratorService;
	private final VotingCardSetGenerateBallotService votingCardSetGenerateBallotService;
	private final ReturnCodesMappingTableFileCreationService returnCodesMappingTableFileCreationService;

	public VotingCardSetGenerateService(final IdleStatusService idleStatusService,
			final VotingCardSetDataGeneratorService votingCardSetDataGeneratorService,
			final VotingCardSetGenerateBallotService votingCardSetGenerateBallotService,
			final ReturnCodesMappingTableFileCreationService returnCodesMappingTableFileCreationService) {
		this.idleStatusService = idleStatusService;
		this.votingCardSetDataGeneratorService = votingCardSetDataGeneratorService;
		this.votingCardSetGenerateBallotService = votingCardSetGenerateBallotService;
		this.returnCodesMappingTableFileCreationService = returnCodesMappingTableFileCreationService;
	}

	/**
	 * Generates the voting card set data based on the given votingCardSetId. The generation contains 3 steps: generate the ballot box data, generate
	 * the ballot file and finally the voting card set data.
	 *
	 * @param electionEventId The id of the election event.
	 * @param votingCardSetId The id of the voting card set for which the data is generated.
	 * @return a DataGeneratorResponse containing information about the result of the generation.
	 * @throws InvalidStatusTransitionException if the original status does not allow the generation
	 */
	public DataGeneratorResponse generate(final String electionEventId, final String votingCardSetId)
			throws ResourceNotFoundException, InvalidStatusTransitionException {

		LOGGER.info("Generating the voting card set. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);

		// Check if generation already started
		if (!idleStatusService.getIdLock(votingCardSetId)) {
			return new DataGeneratorResponse();
		}

		try {

			validateUUID(electionEventId);
			validateUUID(votingCardSetId);

			checkVotingCardSetStatusTransition(electionEventId, votingCardSetId, Status.VCS_DOWNLOADED, Status.GENERATED);

			// Generate the needed data: ballot and ballot box
			final DataGeneratorResponse result = votingCardSetGenerateBallotService.generate(electionEventId, votingCardSetId);
			// rename -> VotingServerDatageneratorservice
			if (!result.isSuccessful()) {
				return result;
			}

			// Generate the Return Codes Mapping Table
			returnCodesMappingTableFileCreationService.generate(electionEventId, votingCardSetId);

			// Generate voting card set data (via spring batch)
			return votingCardSetDataGeneratorService.generate(votingCardSetId, electionEventId);
		} finally {
			idleStatusService.freeIdLock(votingCardSetId);
		}
	}
}
