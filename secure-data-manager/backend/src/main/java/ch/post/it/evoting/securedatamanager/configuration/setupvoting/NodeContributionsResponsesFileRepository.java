/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

/**
 * Allows performing operations with the node contributions responses.
 */
@Repository
public class NodeContributionsResponsesFileRepository {

	private static final Pattern FILE_PATTERN = Pattern.compile("^nodeContributions\\.[\\d]+\\.json$");
	private static final Logger LOGGER = LoggerFactory.getLogger(NodeContributionsResponsesFileRepository.class);

	private final ObjectMapper objectMapper;
	private final PathResolver pathResolver;

	public NodeContributionsResponsesFileRepository(
			final ObjectMapper objectMapper,
			final PathResolver pathResolver) {
		this.objectMapper = objectMapper;
		this.pathResolver = pathResolver;
	}

	/**
	 * Retrieves all node contributions responses corresponding to the given election event id and verification card set id. As the node contributions
	 * responses are separated in chunk files, it returns a {@link List} of node contributions response represented by a
	 * List&lt;ChoiceCodeGenerationDTO&lt;ReturnCodeGenerationResponsePayload&gt;&gt;.
	 *
	 * @param electionEventId       the node contributions responses' election event id. Must be non-null and a valid UUID.
	 * @param verificationCardSetId the node contributions responses' verification card set id. Must be non-null and a valid UUID.
	 * @return all node contributions responses corresponding to the given {@code electionEventId} and {@code verificationCardSetId}.
	 * @throws FailedValidationException if {@code electionEventId} or {@code verificationCardSetId} is invalid.
	 * @throws UncheckedIOException      if the deserialization of the payload fails.
	 */
	public List<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>> findAll(final String electionEventId,
			final String verificationCardSetId) {
		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		final Path verificationCardSetPath = pathResolver.resolveVerificationCardSetPath(electionEventId, verificationCardSetId);
		final Predicate<String> patternPredicate = FILE_PATTERN.asPredicate();

		if (!Files.exists(verificationCardSetPath)) {
			LOGGER.warn("Requested verification card set directory does not exist. [electionEventId: {}, verificationCardSetId: {}, path: {}]",
					electionEventId, verificationCardSetId, verificationCardSetPath);
			return Collections.emptyList();
		}

		try (final Stream<Path> paths = Files.walk(verificationCardSetPath, 1)) {
			return paths.filter(path -> patternPredicate.test(path.getFileName().toString()))
					.parallel()
					.map(chunkFilePath -> {
						try {
							return objectMapper.readValue(chunkFilePath.toFile(),
									new TypeReference<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>>() {
									});
						} catch (final IOException e) {
							throw new UncheckedIOException(
									String.format(
											"Failed to deserialize the node contributions. [electionEventId: %s, verificationCardSetId: %s, path: %s]",
											electionEventId, verificationCardSetId, chunkFilePath), e);
						}
					}).collect(Collectors.toList());
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to walk verification card set directory. [electionEventId: %s, verificationCardSetId: %s, path: %s]",
							electionEventId, verificationCardSetId, verificationCardSetPath), e);
		}
	}

}