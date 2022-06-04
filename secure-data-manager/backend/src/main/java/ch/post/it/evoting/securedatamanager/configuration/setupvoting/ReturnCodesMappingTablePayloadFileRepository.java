/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

@Repository
public class ReturnCodesMappingTablePayloadFileRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesMappingTablePayloadFileRepository.class);

	private static final String FILE_NAME = "returnCodesMappingTablePayload" + Constants.JSON;

	private final ObjectMapper objectMapper;
	private final PathResolver payloadResolver;

	public ReturnCodesMappingTablePayloadFileRepository(
			final ObjectMapper objectMapper,
			final PathResolver payloadResolver) {
		this.objectMapper = objectMapper;
		this.payloadResolver = payloadResolver;
	}

	/**
	 * Saves the return codes mapping table payload to the filesystem for the given election event and verification card set.
	 *
	 * @return the path of the saved file.
	 * @throws NullPointerException     if any of the inputs is null.
	 * @throws IllegalArgumentException if any of the inputs is not valid.
	 * @see PathResolver to get the resolved file Path.
	 */
	public Path save(final ReturnCodesMappingTablePayload payload) {
		checkNotNull(payload);

		final String electionEventId = validateUUID(payload.getElectionEventId());
		final String verificationCardSetId = validateUUID(payload.getVerificationCardSetId());

		final Path payloadPath = payloadPath(electionEventId, verificationCardSetId);

		try {
			final byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);
			final Path filePath = Files.write(payloadPath, payloadBytes);

			LOGGER.debug("Successfully persisted return codes mapping table payload. [electionEventId: {}, verificationCardSetId: {}]",
					electionEventId, verificationCardSetId);

			return filePath;
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Unable to write the return codes mapping table payload file. [electionEventId: %s, verificationCardSetId: %s]",
							electionEventId, verificationCardSetId), e);
		}
	}

	/**
	 * Retrieves from the file system a return codes mapping table payload by election event and verification card set ids.
	 *
	 * @param electionEventId       the payload's election event id.
	 * @param verificationCardSetId the payload's verification card set id.
	 * @return the return codes mapping table payload with the given ids or {@link Optional#empty} if not found.
	 * @throws FailedValidationException if {@code electionEventId} or {@code verificationCardSetId} is invalid.
	 * @throws UncheckedIOException      if the deserialization of the payload fails.
	 */
	public Optional<ReturnCodesMappingTablePayload> findByElectionEventIdAndVerificationCardSetId(final String electionEventId,
			final String verificationCardSetId) {
		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		final Path payloadPath = payloadPath(electionEventId, verificationCardSetId);

		if (!Files.exists(payloadPath)) {
			LOGGER.warn("Requested return codes mapping table payload does not exist. [electionEventId: {}, verificationCardSetId: {}]",
					electionEventId, verificationCardSetId);
			return Optional.empty();
		}

		try {
			return Optional.of(objectMapper.readValue(payloadPath.toFile(), ReturnCodesMappingTablePayload.class));
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to deserialize return codes mapping table payload. [electionEventId: %s, verificationCardSetId: %s]",
							electionEventId, verificationCardSetId), e);
		}
	}

	private Path payloadPath(final String electionEventId, final String verificationCardSetId) {
		final Path verificationCardSetPath = payloadResolver.resolveVerificationCardSetPath(electionEventId, verificationCardSetId);
		return verificationCardSetPath.resolve(FILE_NAME);
	}
}
