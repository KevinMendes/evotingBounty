/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetPayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

public abstract class MixnetPayloadFileRepository {

	protected final String filePrefix;
	protected final ObjectMapper objectMapper;
	protected final PathResolver payloadResolver;

	protected MixnetPayloadFileRepository(
			final String filePrefix, final ObjectMapper objectMapper, final PathResolver payloadResolver) {
		this.filePrefix = filePrefix;
		this.objectMapper = objectMapper;
		this.payloadResolver = payloadResolver;
	}

	/**
	 * Saves the mixnet payload to the filesystem for the given election event, ballot, ballot box, control component combination.
	 *
	 * @return the path of the saved file.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the inputs is not valid.
	 * @see PathResolver to get the resolved file Path.
	 */
	protected Path savePayload(final String electionEventId, final String ballotId, final String ballotBoxId, final MixnetPayload payload,
			final int nodeId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		checkNotNull(payload);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);

		final Path payloadPath = payloadPath(electionEventId, ballotId, ballotBoxId, nodeId);

		try {
			final Path filePath = Files.createFile(payloadPath);
			objectMapper.writeValue(filePath.toFile(), payload);
			return filePath;
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to write the mixnet payload file", e);
		}
	}

	protected Path payloadPath(final String electionEventId, final String ballotId, final String ballotBoxId, final int nodeId) {
		final Path ballotBoxPath = payloadResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId);
		return ballotBoxPath.resolve(filePrefix + nodeId + Constants.JSON);
	}
}
