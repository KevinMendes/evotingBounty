/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static java.nio.file.Files.delete;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.newOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;

import javax.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * This is an application service that manages voting card sets.
 */
@Service
public class VotingCardSetDownloadService extends BaseVotingCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetDownloadService.class);

	private final IdleStatusService idleStatusService;
	private final VotingCardSetChoiceCodesService votingCardSetChoiceCodesService;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	public VotingCardSetDownloadService(
			final IdleStatusService idleStatusService,
			final VotingCardSetChoiceCodesService votingCardSetChoiceCodesService,
			final ConfigurationEntityStatusService configurationEntityStatusService,
			final ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository) {
		this.idleStatusService = idleStatusService;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.votingCardSetChoiceCodesService = votingCardSetChoiceCodesService;
		this.returnCodeGenerationRequestPayloadRepository = returnCodeGenerationRequestPayloadRepository;
	}

	/**
	 * Download the computed values for a votingCardSet
	 *
	 * @throws InvalidStatusTransitionException if the original status does not allow the download
	 */
	public void download(final String votingCardSetId, final String electionEventId)
			throws ResourceNotFoundException, InvalidStatusTransitionException, IOException {

		LOGGER.info("Downloading the computed values. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);

		if (!idleStatusService.getIdLock(votingCardSetId)) {
			return;
		}

		try {
			final Status fromStatus = Status.COMPUTED;
			final Status toStatus = Status.VCS_DOWNLOADED;

			checkVotingCardSetStatusTransition(electionEventId, votingCardSetId, fromStatus, toStatus);

			final JsonObject votingCardSetJson = votingCardSetRepository.getVotingCardSetJson(electionEventId, votingCardSetId);
			final String verificationCardSetId = votingCardSetJson.getString(JsonConstants.VERIFICATION_CARD_SET_ID);

			deleteNodeContributions(electionEventId, verificationCardSetId);

			final int chunkCount;
			try {
				chunkCount = returnCodeGenerationRequestPayloadRepository.getCount(electionEventId, verificationCardSetId);
			} catch (final PayloadStorageException e) {
				throw new IllegalStateException("Failed to get the chunk count.", e);
			}

			for (int i = 0; i < chunkCount; i++) {
				try (final InputStream contributions = votingCardSetChoiceCodesService.download(electionEventId, verificationCardSetId, i)) {
					writeNodeContributions(electionEventId, verificationCardSetId, i, contributions);
				}
			}

			configurationEntityStatusService.update(toStatus.name(), votingCardSetId, votingCardSetRepository);

		} finally {
			idleStatusService.freeIdLock(votingCardSetId);
		}

	}

	private static boolean isNodeContributions(final Path file) {
		final String name = file.getFileName().toString();
		return name.startsWith(Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS) && name.endsWith(Constants.JSON);
	}

	private void deleteNodeContributions(final String electionEventId, final String verificationCardSetId) throws IOException {
		final Path folder = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId);
		final Filter<Path> filter = VotingCardSetDownloadService::isNodeContributions;
		try (final DirectoryStream<Path> files = newDirectoryStream(folder, filter)) {
			for (final Path file : files) {
				delete(file);
			}
		}
	}

	private void writeNodeContributions(final String electionEventId, final String verificationCardSetId, final int chunkId,
			final InputStream contributions)
			throws IOException {
		final String fileName = Constants.CONFIG_FILE_NAME_NODE_CONTRIBUTIONS + "." + chunkId + Constants.JSON;
		final Path file = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_VOTEVERIFICATION).resolve(verificationCardSetId).resolve(fileName);

		try (final OutputStream stream = newOutputStream(file)) {
			IOUtils.copy(contributions, stream);
		}
	}
}
