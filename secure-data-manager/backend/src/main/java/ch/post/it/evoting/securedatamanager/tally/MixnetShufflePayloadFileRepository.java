/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@Repository
public class MixnetShufflePayloadFileRepository extends MixnetPayloadFileRepository {

	@Autowired
	public MixnetShufflePayloadFileRepository(
			@Value("${shufflePayload.filePrefix:mixnetShufflePayload_}")
			final String filePrefix, final ObjectMapper objectMapper, final PathResolver payloadResolver) {
		super(filePrefix, objectMapper, payloadResolver);
	}

	/**
	 * Gets the mixnet shuffle payload stored on the filesystem for the given election event, ballot, ballot box, control component combination.
	 *
	 * @return the MixnetShufflePayload object read from the stored file.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the inputs is not valid.
	 * @see PathResolver to get the resolved file Path.
	 */
	public MixnetShufflePayload getPayload(final String electionEventId, final String ballotId, final String ballotBoxId,
			final int controlComponentNodeId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotId);
		checkNotNull(ballotBoxId);
		validateUUID(electionEventId);
		validateUUID(ballotId);
		validateUUID(ballotBoxId);
		checkArgument(controlComponentNodeId >= 1 && controlComponentNodeId <= 3);

		final Path payloadPath = payloadPath(electionEventId, ballotId, ballotBoxId, controlComponentNodeId);

		try {
			return objectMapper.readValue(payloadPath.toFile(), MixnetShufflePayload.class);
		} catch (final IOException e) {
			throw new UncheckedIOException("Unable to read the mixnet payload file", e);
		}
	}

	/**
	 * Saves the mixnet shuffle payload to the filesystem for the given election event, ballot, ballot box, control component combination.
	 *
	 * @return the path of the saved file.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the inputs is not valid.
	 * @see PathResolver to get the resolved file Path.
	 */
	public Path savePayload(final String electionEventId, final String ballotId, final String ballotBoxId, final MixnetShufflePayload payload) {
		return super.savePayload(electionEventId, ballotId, ballotBoxId, payload, payload.getNodeId());
	}
}
