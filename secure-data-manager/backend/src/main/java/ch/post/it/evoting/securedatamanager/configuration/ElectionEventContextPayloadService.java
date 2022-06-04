/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;

/**
 * Allows saving, retrieving and finding existing election event context payloads.
 */
@Service
public class ElectionEventContextPayloadService {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextPayloadService.class);

	private final ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepository;

	public ElectionEventContextPayloadService(final ElectionEventContextPayloadFileRepository electionEventContextPayloadFileRepository) {
		this.electionEventContextPayloadFileRepository = electionEventContextPayloadFileRepository;
	}

	/**
	 * Saves an election event context payload in the corresponding election event folder.
	 *
	 * @param electionEventContextPayload the election event context payload to save.
	 * @throws NullPointerException if {@code electionEventContext} is null.
	 */
	public void save(final ElectionEventContextPayload electionEventContextPayload) {
		checkNotNull(electionEventContextPayload);

		final String electionEventId = electionEventContextPayload.getElectionEventContext().getElectionEventId();

		electionEventContextPayloadFileRepository.save(electionEventContextPayload);
		LOGGER.info("Saved election event context payload. [electionEventId: {}]", electionEventId);
	}

	/**
	 * Checks if the election event context payload is present for the given election event id.
	 *
	 * @param electionEventId the election event id to check.
	 * @return {@code true} if the election event context payload is present, {@code false} otherwise.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 */
	public boolean exist(final String electionEventId) {
		validateUUID(electionEventId);

		return electionEventContextPayloadFileRepository.existsById(electionEventId);
	}

	/**
	 * Loads the election event context payload for the given {@code electionEventId}. The result of this method is cached.
	 *
	 * @param electionEventId the election event id.
	 * @return the election event context payload for this {@code electionEventId}.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalStateException     if the requested election event context is not present.
	 */
	@Cacheable("electionEventContextPayload")
	public ElectionEventContextPayload load(final String electionEventId) {
		validateUUID(electionEventId);

		final ElectionEventContextPayload payload = electionEventContextPayloadFileRepository.findById(electionEventId)
				.orElseThrow(() -> new IllegalStateException(
						String.format("Requested election event context payload is not present. [electionEventId: %s]", electionEventId)));

		LOGGER.info("Loaded election event context payload. [electionEventId: {}]", electionEventId);

		return payload;
	}

}
