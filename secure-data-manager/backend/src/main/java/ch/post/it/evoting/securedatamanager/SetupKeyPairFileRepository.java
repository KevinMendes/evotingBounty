/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

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
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

/**
 * Allows performing operations with the setup key pair of an election. The key pair are retrieved from the file system of the SDM, in its workspace.
 */
@Repository
public class SetupKeyPairFileRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(SetupKeyPairFileRepository.class);

	private final ObjectMapper objectMapper;
	private final PathResolver pathResolver;
	private final EncryptionParametersFileRepository encryptionParametersFileRepository;

	public SetupKeyPairFileRepository(
			final ObjectMapper objectMapper,
			final PathResolver pathResolver,
			final EncryptionParametersFileRepository encryptionParametersFileRepository) {
		this.objectMapper = objectMapper;
		this.pathResolver = pathResolver;
		this.encryptionParametersFileRepository = encryptionParametersFileRepository;
	}

	/**
	 * Persists a setup key pair to the file system.
	 *
	 * @param electionEventId the election event id for which to persist the key pair.
	 * @param setupKeyPair    the key pair to persist.
	 * @return the path where the key pair has been successfully persisted.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws NullPointerException      if {@code setupKeyPair} is null.
	 * @throws UncheckedIOException      if the serialization of the key pair failed.
	 */
	public Path save(final String electionEventId, final ElGamalMultiRecipientKeyPair setupKeyPair) {
		validateUUID(electionEventId);
		checkNotNull(setupKeyPair);

		final Path offlinePath = pathResolver.resolveOfflinePath(electionEventId);
		final Path setupKeyPairPath = offlinePath.resolve(Constants.SETUP_KEY_PAIR_FILE_NAME);

		try {
			final Path writePath = Files.write(setupKeyPairPath, objectMapper.writeValueAsBytes(setupKeyPair));
			LOGGER.debug("Successfully persisted setup key pair. [electionEventId: {}, path: {}]", electionEventId, setupKeyPairPath);

			return writePath;
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to serialize setup key pair. [electionEventId: %s, path: %s]", electionEventId, setupKeyPairPath), e);
		}
	}

	/**
	 * Retrieves from the file system a setup key pair by election event id.
	 *
	 * @param electionEventId the election event if for which to retrieve the key pair.
	 * @return the setup key pair with the given id or {@link Optional#empty()} if none found.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalStateException     if no corresponding encryption parameters are found.
	 * @throws UncheckedIOException      if the deserialization of the key pair failed.
	 */
	public Optional<ElGamalMultiRecipientKeyPair> findById(final String electionEventId) {
		validateUUID(electionEventId);

		final Path offlinePath = pathResolver.resolveOfflinePath(electionEventId);
		final Path setupKeyPairPath = offlinePath.resolve(Constants.SETUP_KEY_PAIR_FILE_NAME);

		if (!Files.exists(setupKeyPairPath)) {
			LOGGER.debug("Requested setup key pair does not exist. [electionEventId: {}, path: {}]", electionEventId, setupKeyPairPath);
			return Optional.empty();
		}

		final GqGroup gqGroup = encryptionParametersFileRepository.load(electionEventId)
				.orElseThrow(
						() -> new IllegalStateException(String.format("Encryption parameters not found. [electionEventId: %s]", electionEventId)));

		try {
			return Optional.of(objectMapper.reader()
					.withAttribute("group", gqGroup)
					.readValue(setupKeyPairPath.toFile(), ElGamalMultiRecipientKeyPair.class));
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to deserialize setup key pair. [electionEventId: %s, path: %s]", electionEventId, setupKeyPairPath), e);
		}
	}

}
