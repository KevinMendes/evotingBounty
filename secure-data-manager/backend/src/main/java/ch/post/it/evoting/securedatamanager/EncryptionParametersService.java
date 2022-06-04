/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

/**
 * Allows loading the encryption parameters associated to an election event.
 */
@Service
public class EncryptionParametersService {

	private final EncryptionParametersFileRepository encryptionParametersFileRepository;

	public EncryptionParametersService(
			final EncryptionParametersFileRepository encryptionParametersFileRepository) {
		this.encryptionParametersFileRepository = encryptionParametersFileRepository;
	}

	/**
	 * Loads the encryption parameters for the given {@code electionEventId}. The result of this method is cached.
	 *
	 * @param electionEventId the election event id for which to get the encryption parameters.
	 * @return the encryption parameters.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalArgumentException  if the encryption parameters are not found for this {@code electionEventId}.
	 */
	@Cacheable("gqGroups")
	public GqGroup load(final String electionEventId) {
		validateUUID(electionEventId);

		return encryptionParametersFileRepository.load(electionEventId)
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Encryption parameters not found. [electionEventId: %s]", electionEventId)));
	}

}
