/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

/**
 * Allows performing operations with the control component public keys payloads. The payloads are persisted/retrieved to/from the file system of the
 * SDM, in its workspace.
 */
@Repository
public class ControlComponentPublicKeysPayloadFileRepository {

	@VisibleForTesting
	static final String PAYLOAD_FILE_NAME = "controlComponentPublicKeysPayload.%s.json";

	private static final Pattern PAYLOAD_FILE_PATTERN = Pattern.compile("^controlComponentPublicKeysPayload\\.[\\d]\\.json$");
	private static final Logger LOGGER = LoggerFactory.getLogger(ControlComponentPublicKeysPayloadFileRepository.class);

	private final ObjectMapper objectMapper;
	private final PathResolver pathResolver;

	public ControlComponentPublicKeysPayloadFileRepository(
			final ObjectMapper objectMapper, final PathResolver pathResolver) {
		this.objectMapper = objectMapper;
		this.pathResolver = pathResolver;
	}

	/**
	 * Persists a control component public keys payload to the file system.
	 *
	 * @param controlComponentPublicKeysPayload the payload to persist.
	 * @return the path where the payload has been successfully persisted.
	 * @throws NullPointerException if {@code controlComponentPublicKeysPayload} is null.
	 * @throws UncheckedIOException if the serialization of the payload fails.
	 */
	public Path save(final ControlComponentPublicKeysPayload controlComponentPublicKeysPayload) {
		checkNotNull(controlComponentPublicKeysPayload);

		final String electionEventId = controlComponentPublicKeysPayload.getElectionEventId();
		final int nodeId = controlComponentPublicKeysPayload.getControlComponentPublicKeys().getNodeId();

		final Path electionEventPath = pathResolver.resolveElectionEventPath(electionEventId);
		final Path payloadPath = electionEventPath.resolve(String.format(PAYLOAD_FILE_NAME, nodeId));

		final byte[] payloadBytes;
		try {
			payloadBytes = objectMapper.writeValueAsBytes(controlComponentPublicKeysPayload);

			final Path writePath = Files.write(payloadPath, payloadBytes);
			LOGGER.debug("Successfully persisted control component public keys payloads. [electionEventId: {}, nodeId: {}, path: {}]",
					electionEventId, nodeId, payloadPath);

			return writePath;
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to serialize control component public keys payload. [electionEventId: %s, nodeId: %s, path: %s]",
							electionEventId, nodeId, payloadPath), e);
		}
	}

	/**
	 * Checks if the control component public keys payload file exists for the given {@code electionEventId} and {@code nodeId}.
	 *
	 * @param electionEventId the election event id to check.
	 * @param nodeId          the node id to check.
	 * @return {@code true} if the payload file exists, {@code false} otherwise.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 */
	public boolean existsById(final String electionEventId, final int nodeId) {
		validateUUID(electionEventId);

		final Path electionEventPath = pathResolver.resolveElectionEventPath(electionEventId);
		final Path payloadPath = electionEventPath.resolve(String.format(PAYLOAD_FILE_NAME, nodeId));
		LOGGER.debug("Checking control component public keys payload file existence. [electionEventId: {}, nodeId: {}, path: {}]", electionEventId,
				nodeId, payloadPath);

		return Files.exists(payloadPath);
	}

	/**
	 * Retrieves from the file system a control component public keys payload by election event and node ids.
	 *
	 * @param electionEventId the payload's election event id.
	 * @param nodeId          the payload's node id.
	 * @return the control component public keys payload with the given ids or {@link Optional#empty} if none found.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws UncheckedIOException      if the deserialization of the payload fails.
	 */
	public Optional<ControlComponentPublicKeysPayload> findById(final String electionEventId, final int nodeId) {
		validateUUID(electionEventId);

		final Path electionEventPath = pathResolver.resolveElectionEventPath(electionEventId);
		final Path payloadPath = electionEventPath.resolve(String.format(PAYLOAD_FILE_NAME, nodeId));

		if (!Files.exists(payloadPath)) {
			LOGGER.debug("Requested payload does not exist. [electionEventId: {}, nodeId: {}, path: {}]", electionEventId, nodeId, payloadPath);
			return Optional.empty();
		}

		try {
			return Optional.of(objectMapper.readValue(payloadPath.toFile(), ControlComponentPublicKeysPayload.class));
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to deserialize control component public keys payload. [electionEventId: %s, nodeId: %s, path: %s]",
							electionEventId, nodeId, payloadPath), e);
		}
	}

	/**
	 * Retrieves all control component public keys payloads corresponding to the given election event id. The returned list is ordered by the {@link
	 * ControlComponentPublicKeys}.nodeId.
	 *
	 * @param electionEventId the election event id for which to retrieve the payloads.
	 * @return all payloads for {@code electionEventId} ordered by node id.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws UncheckedIOException      if the deserialization of the payload fails.
	 */
	public List<ControlComponentPublicKeysPayload> findAllOrderByNodeId(final String electionEventId) {
		validateUUID(electionEventId);

		final Path electionEventPath = pathResolver.resolveElectionEventPath(electionEventId);
		final Predicate<String> patternPredicate = PAYLOAD_FILE_PATTERN.asPredicate();

		if (!Files.exists(electionEventPath)) {
			LOGGER.debug("Request election event does not exist. [electionEventId: {}, path: {}]", electionEventId, electionEventPath);
			return Collections.emptyList();
		}

		try (final Stream<Path> paths = Files.walk(electionEventPath, 1)) {
			return paths.filter(path -> patternPredicate.test(path.getFileName().toString()))
					.map(payloadPath -> {
						try {
							return objectMapper.readValue(payloadPath.toFile(), ControlComponentPublicKeysPayload.class);
						} catch (final IOException e) {
							throw new UncheckedIOException(
									String.format("Failed to deserialize control component public keys payload. [electionEventId: %s, path: %s]",
											electionEventId, payloadPath), e);
						}
					})
					.sorted(Comparator.comparingInt(e -> e.getControlComponentPublicKeys().getNodeId()))
					.collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to walk election event directory. [electionEventId: %s, path: %s]", electionEventId, electionEventPath), e);
		}
	}

}
