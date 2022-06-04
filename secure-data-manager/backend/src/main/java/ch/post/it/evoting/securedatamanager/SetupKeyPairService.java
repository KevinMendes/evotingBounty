/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;

/**
 * Allows saving and retrieving setup key pairs.
 */
@Service
public class SetupKeyPairService {

	private static final Logger LOGGER = getLogger(SetupKeyPairService.class);

	private final SetupKeyPairFileRepository setupKeyPairFileRepository;

	public SetupKeyPairService(final SetupKeyPairFileRepository setupKeyPairFileRepository) {
		this.setupKeyPairFileRepository = setupKeyPairFileRepository;
	}

	/**
	 * Saves a setup key pair in the corresponding election event folder.
	 *
	 * @param electionEventId the election event id for which to save the key pair.
	 * @param setupKeyPair    the key pair to save.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws NullPointerException      if {@code setupKeyPair} is null.
	 */
	public void save(final String electionEventId, final ElGamalMultiRecipientKeyPair setupKeyPair) {
		validateUUID(electionEventId);
		checkNotNull(setupKeyPair);

		setupKeyPairFileRepository.save(electionEventId, setupKeyPair);
		LOGGER.info("Saved setup key pair. [electionEventId: {}]", electionEventId);
	}

	/**
	 * Loads the setup key pair for the given election event id. The result of this method is cached.
	 *
	 * @param electionEventId the election event id for which to load the key pair.
	 * @return the setup key pair.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalArgumentException  if the setup key pair for this {@code electionEventId} is not found.
	 */
	@Cacheable("setupKeyPairs")
	public ElGamalMultiRecipientKeyPair load(final String electionEventId) {
		validateUUID(electionEventId);

		return setupKeyPairFileRepository.findById(electionEventId)
				.orElseThrow(() -> new IllegalArgumentException(
						String.format("Setup key pair not found. [electionEventId: %s]", electionEventId)));
	}
}
