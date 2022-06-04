/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.post.it.evoting.controlcomponents.keymanagement.LockKey;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;

@Service
public class VerificationCardSetService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationCardSetService.class);

	private final LockRegistry lockRegistry;
	private final ElectionEventService electionEventService;
	private final VerificationCardSetRepository verificationCardSetRepository;

	public VerificationCardSetService(final LockRegistry lockRegistry,
			final ElectionEventService electionEventService,
			final VerificationCardSetRepository verificationCardSetRepository) {
		this.lockRegistry = lockRegistry;
		this.electionEventService = electionEventService;
		this.verificationCardSetRepository = verificationCardSetRepository;
	}

	@Transactional
	public VerificationCardSetEntity save(final VerificationCardSetEntity verificationCardSetEntity) {
		checkNotNull(verificationCardSetEntity);

		return verificationCardSetRepository.save(verificationCardSetEntity);
	}

	@Transactional
	public boolean exists(final String verificationCardSetId) {
		validateUUID(verificationCardSetId);

		return verificationCardSetRepository.existsByVerificationCardSetId(verificationCardSetId);
	}

	@Transactional
	public VerificationCardSetEntity getVerificationCardSet(final String verificationCardSetId) {
		validateUUID(verificationCardSetId);

		return verificationCardSetRepository.findByVerificationCardSetId(verificationCardSetId)
				.orElseThrow(() -> new IllegalStateException(
						String.format("Verification card set not found. [verificationCardSetId: %s]", verificationCardSetId)));
	}

	@Transactional
	@Retryable(value = TimeoutException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2, random = true))
	public VerificationCardSetEntity updateAllowList(final String electionEventId, final String verificationCardSetId,
			final List<String> payloadAllowList, final CombinedCorrectnessInformation combinedCorrectnessInformation) throws TimeoutException {

		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);
		checkNotNull(payloadAllowList);

		//A node level lock across all instances for this verificationCardSetId.
		final Lock nodeLock = lockRegistry.obtain(LockKey.NODE_KEY.getKey() + "-" + verificationCardSetId);

		final boolean lockAcquired;
		try {
			lockAcquired = nodeLock.tryLock(5, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CompletionException(
					String.format("Failed to acquire lock on verification card set. [verificationCardSetId: %s]", verificationCardSetId), e);
		}

		if (!lockAcquired) {
			LOGGER.warn("Failed to acquire lock for [verificationCardSetId: {}]", verificationCardSetId);
			throw new TimeoutException("Failed to acquire node lock");
		}

		// Retrieve or create verification card set if it is the first chunk.
		try {
			if (exists(verificationCardSetId)) {
				final VerificationCardSetEntity verificationCardSetEntity = getVerificationCardSet(verificationCardSetId);

				// Update allow list.
				final List<String> existingAllowList = verificationCardSetEntity.getAllowList();
				final List<String> mergedAllowList = new ArrayList<>(existingAllowList);
				mergedAllowList.addAll(payloadAllowList);
				verificationCardSetEntity.setAllowList(mergedAllowList);

				final VerificationCardSetEntity savedVerificationCardSetEntity = save(verificationCardSetEntity);
				LOGGER.info("Allow list updated. [electionEventId: {}, verificationCardSetId: {}]", electionEventId, verificationCardSetId);

				return savedVerificationCardSetEntity;
			} else {
				// Create a new verification card set.
				final ElectionEventEntity electionEventEntity = electionEventService.getElectionEventEntity(electionEventId);
				final VerificationCardSetEntity newVerificationCardSetEntity = new VerificationCardSetEntity(verificationCardSetId,
						electionEventEntity);

				// Set combined correctness information.
				newVerificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);

				// Set allow list.
				newVerificationCardSetEntity.setAllowList(payloadAllowList);

				final VerificationCardSetEntity savedVerificationCardSetEntity = save(newVerificationCardSetEntity);
				LOGGER.info("New verification card set saved. [electionEventId: {}, verificationCardSetId: {}]", electionEventId,
						verificationCardSetId);

				return savedVerificationCardSetEntity;
			}
		} finally {
			nodeLock.unlock();
		}
	}

}
