/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetFinalPayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@Repository
public class MixnetFinalPayloadFileRepository {

	private final String fileName;
	private final ObjectMapper objectMapper;
	private final PathResolver payloadResolver;

	@Autowired
	public MixnetFinalPayloadFileRepository(
			@Value("${finalPayload.fileName:mixnetFinalPayload_4}")
			final String fileName, final ObjectMapper objectMapper, final PathResolver payloadResolver) {

		this.fileName = fileName;
		this.objectMapper = objectMapper;
		this.payloadResolver = payloadResolver;
	}

	/**
	 * Gets the mixnet payload stored on the filesystem for the given election, ballot and ballot box ids.
	 *
	 * @param electionEventId valid election event id. Must be non null.
	 * @param ballotId        valid ballot id. Must be non null.
	 * @param ballotBoxId     valid ballot box id. Must be non null.
	 * @return the {@link MixnetFinalPayload} object read from the stored file.
	 * @see PathResolver to get the resolved file Path.
	 */
	public MixnetFinalPayload getPayload(final String electionEventId, final String ballotId, final String ballotBoxId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		final Path payloadPath = payloadPath(electionEventId, ballotId, ballotBoxId);

		try {
			return objectMapper.readValue(payloadPath.toFile(), MixnetFinalPayload.class);
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to read the mixnet payload file", e);
		}
	}

	/**
	 * Saves the mixnet payload to the filesystem for the given election, ballot, and ballot box ids combination.
	 *
	 * @param electionEventId valid election event id. Must be non null.
	 * @param ballotId        valid ballot id. Must be non null.
	 * @param ballotBoxId     valid ballot box id. Must be non null.
	 * @param payload         the {@link MixnetFinalPayload} to persist.
	 * @return the path of the saved file.
	 * @see PathResolver to get the resolved file Path.
	 */
	public Path savePayload(final String electionEventId, final String ballotId, final String ballotBoxId, final MixnetFinalPayload payload) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		checkNotNull(payload);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		final Path payloadPath = payloadPath(electionEventId, ballotId, ballotBoxId);

		try {
			final Path filePath = Files.createFile(payloadPath);
			objectMapper.writeValue(filePath.toFile(), payload);
			return filePath;
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to write the mixnet payload file", e);
		}
	}

	@VisibleForTesting
	Path payloadPath(final String electionEventId, final String ballotId, final String ballotBoxId) {
		final Path ballotBoxPath = payloadResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);
		return ballotBoxPath.resolve(fileName + Constants.JSON);
	}
}
