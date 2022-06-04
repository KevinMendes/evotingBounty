/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.services.application.service.ConsistencyCheckService;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;

/**
 * This service accesses a generator of ballot data.
 */
@Service
public class BallotDataGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotDataGeneratorService.class);
	private final BallotRepository repository;
	private final PathResolver resolver;
	private final ConsistencyCheckService consistencyCheckService;

	/**
	 * Constructor.
	 *
	 * @param repository
	 * @param resolver
	 */
	@Autowired
	public BallotDataGeneratorService(final BallotRepository repository, final PathResolver resolver, final ConsistencyCheckService consistencyCheckService) {
		this.repository = repository;
		this.resolver = resolver;
		this.consistencyCheckService = consistencyCheckService;
	}

	/**
	 * This method generates all the data for a ballot.
	 *
	 * @param id              The identifier of the ballot set for which to generate the data.
	 * @param electionEventId The identifier of the election event to whom this ballot set belongs.
	 * @return a bean containing information about the result of the generation.
	 */
	public DataGeneratorResponse generate(final String id, final String electionEventId) {
		final DataGeneratorResponse result = new DataGeneratorResponse();

		// basic validation of input
		if (StringUtils.isEmpty(id)) {
			result.setSuccessful(false);
			return result;
		}

		// read the ballot from the database
		final String json = repository.find(id);

		// simple check if there is a voting card set data returned
		if (JsonConstants.EMPTY_OBJECT.equals(json)) {
			result.setSuccessful(false);
			return result;
		}

		// check consistency for the ballot
		try {
			// Get the representations file for the Election Event
			final Path representationsFile = resolver
					.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_CUSTOMER,
							Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV);
			if (!consistencyCheckService.representationsConsistent(json, representationsFile)) {
				final String errMsg = "Consistency check of the representations used on the ballot options failed.";
				LOGGER.error(errMsg);
				result.setResult(errMsg);
				result.setSuccessful(false);
				return result;
			}
		} catch (final IOException e) {
			LOGGER.error("Failed to read representations file.", e);
			result.setSuccessful(false);
		}

		final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

		final Path file = resolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION).resolve(Constants.CONFIG_DIR_NAME_BALLOTS).resolve(id)
				.resolve(Constants.CONFIG_FILE_NAME_BALLOT_JSON);
		try {
			createDirectories(file.getParent());
			// ballot.json must not be written for each ballot box from the
			// ballot because already working voting card generator can read
			// corrupted content, thus ballot.json must be written only once.
			// Write ballot.json only if the file does not exist or has a
			// different content. The content is pretty small, so it is possible
			// compare everything in memory. Synchronized block protects the
			// operation from a concurrent call, because services in Spring are
			// normally singletons.
			synchronized (this) {
				if (!exists(file) || !Arrays.equals(bytes, readAllBytes(file))) {
					write(file, bytes);
				}
			}
		} catch (final IOException e) {
			LOGGER.error("Failed to write ballot to file.", e);
			result.setSuccessful(false);
		}
		return result;
	}

	/**
	 * Removes all the ballot.json files inside sdm/config/{electionEventId}/ONLINE/electionInformation/ballots/ This method is used to later
	 * re-generate those files, calling the generate method for each ballot
	 *
	 * @param electionEventId The election event of the ballots to be removed
	 */
	public void cleanAll(final String electionEventId) {
		final Path ballotsPath = resolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId).resolve(Constants.CONFIG_DIR_NAME_ONLINE)
				.resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION).resolve(Constants.CONFIG_DIR_NAME_BALLOTS);

		if (!exists(ballotsPath)) {
			throw new IllegalStateException("Ballot files have not been generated yet");
		}

		final File[] ballotPath = ballotsPath.toFile().listFiles();

		for (final File ballotFolder : ballotPath) {
			if (ballotFolder.isDirectory()) {
				final Path file = ballotFolder.toPath().resolve(Constants.CONFIG_FILE_NAME_BALLOT_JSON);
				try {
					Files.deleteIfExists(file);
				} catch (final IOException e) {
					LOGGER.error("Failed to delete ballot file.", e);
				}
			}
		}
	}
}
