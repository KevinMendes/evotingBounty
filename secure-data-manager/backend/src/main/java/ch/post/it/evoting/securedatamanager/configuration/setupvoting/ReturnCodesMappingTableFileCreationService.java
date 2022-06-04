/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.securedatamanager.task.FileCreationTaskService;
import ch.post.it.evoting.securedatamanager.task.FileCreationTaskType;

@Service
public class ReturnCodesMappingTableFileCreationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesMappingTableFileCreationService.class);

	private final FileCreationTaskService fileCreationTaskService;
	private final ReturnCodesMappingTablePayloadService returnCodesMappingTablePayloadService;

	public ReturnCodesMappingTableFileCreationService(
			final FileCreationTaskService fileCreationTaskService,
			final ReturnCodesMappingTablePayloadService returnCodesMappingTablePayloadService) {
		this.fileCreationTaskService = fileCreationTaskService;
		this.returnCodesMappingTablePayloadService = returnCodesMappingTablePayloadService;
	}

	public void generate(final String electionEventId, final String votingCardSetId) {
		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		fileCreationTaskService.executeFileCreationTask(votingCardSetId, FileCreationTaskType.CM_TABLE,
				"Return codes mapping table file creation task", () -> fileCreation(electionEventId, votingCardSetId));
	}

	private void fileCreation(final String electionEventId, final String votingCardSetId) {
		final ReturnCodesMappingTablePayload returnCodesMappingTablePayload = returnCodesMappingTablePayloadService.generate(electionEventId,
				votingCardSetId);
		returnCodesMappingTablePayloadService.save(returnCodesMappingTablePayload);

		LOGGER.info("Return codes mapping table file successfully created and saved. [electionEventId: {}, votingCardSetId: {}]", electionEventId,
				votingCardSetId);
	}

}
