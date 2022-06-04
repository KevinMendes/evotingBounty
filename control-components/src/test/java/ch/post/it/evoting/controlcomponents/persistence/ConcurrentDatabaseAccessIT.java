/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.google.common.base.Strings;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;
import ch.post.it.evoting.controlcomponents.ElectionEventRepository;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.TestDatabaseCleanUpService;
import ch.post.it.evoting.controlcomponents.VerificationCard;
import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateEntity;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateRepository;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

/**
 * These test are here to simulate multiple JVMs accessing the same shared resources on the database. This is the case when the control components are
 * replicated over multiple machines with the same service logic. The first tests whether two simultaneous insert leads to the second one overwriting
 * the first. Without optimistic locking JPA save becomes an insert or update so the second transaction to go through overwrites the first. We saw
 * this bug in testing. The second test is to make sure that two simultaneous updates to the same record are atomic, ie doesn't lead to an
 * inconsistent state. Optimistic locking put in place guarantees that.
 */
@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ConcurrentDatabaseAccessIT {

	private static final String electionEventId = Strings.padEnd("", 32, '0');
	private static final String verificationCardSetId = Strings.padEnd("", 32, '0');
	private static final String verificationCardId = Strings.padEnd("", 32, '0');
	private static final GqGroup encryptionGroup = GroupTestData.getGqGroup();

	@Autowired
	private VerificationCardStateRepository verificationCardStateRepository;

	@Autowired
	private ElectionEventRepository electionEventRepository;

	@Autowired
	private ElectionEventService electionEventService;

	@Autowired
	private VerificationCardSetService verificationCardSetService;

	@Autowired
	private VerificationCardService verificationCardService;

	@AfterEach
	void cleanUp(
			@Autowired
			final TestDatabaseCleanUpService testDatabaseCleanUpService) {

		testDatabaseCleanUpService.cleanUp();
	}

	@RepeatedTest(5)
	void testPrimaryKeyViolation() throws InterruptedException {
		final int numThreads = 2;
		final CountDownLatch checkLatch = new CountDownLatch(numThreads);
		final CountDownLatch saveLatch = new CountDownLatch(1);

		final ExecutorService executorService = Executors.newFixedThreadPool(numThreads, new CustomizableThreadFactory("lock-"));

		final CompletableFuture<?> saveFirst = CompletableFuture.runAsync(
				checkTogetherThenSaveFirst(checkLatch, saveLatch),
				executorService);
		final CompletableFuture<?> saveSecond = CompletableFuture.runAsync(
				checkTogetherThenSaveSecond(checkLatch, saveLatch),
				executorService);

		executorService.shutdown();
		final boolean normalTermination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertTrue(normalTermination);

		assertFalse(saveFirst.isCompletedExceptionally());
		assertTrue(saveSecond.isCompletedExceptionally());

		final Optional<ElectionEventEntity> electionEventEntity = electionEventRepository.findByElectionEventId(electionEventId);
		assertTrue(electionEventEntity.isPresent());
		assertEquals(encryptionGroup, electionEventEntity.get().getEncryptionGroup());

		final Throwable cause = getCause(saveSecond);
		assertInstanceOf(DataIntegrityViolationException.class, cause);
	}

	Runnable checkTogetherThenSaveFirst(final CountDownLatch checkLatch, final CountDownLatch saveLatch) {
		return () -> {
			if (!electionEventRepository.findByElectionEventId(electionEventId).isPresent()) {
				checkLatch.countDown();

				awaitOneSecondWithRuntimeException(checkLatch);

				final ElectionEventEntity electionEventEntity = new ElectionEventEntity(electionEventId, encryptionGroup);
				electionEventRepository.save(electionEventEntity);
				saveLatch.countDown();
			}
		};
	}

	Runnable checkTogetherThenSaveSecond(final CountDownLatch checkLatch, final CountDownLatch saveLatch) {
		return () -> {
			if (!electionEventRepository.findByElectionEventId(electionEventId).isPresent()) {
				checkLatch.countDown();

				awaitOneSecondWithRuntimeException(checkLatch);
				awaitOneSecondWithRuntimeException(saveLatch);

				final ElectionEventEntity electionEventEntity = new ElectionEventEntity(electionEventId, encryptionGroup);
				electionEventRepository.save(electionEventEntity);
			}
		};
	}

	@RepeatedTest(5)
	void testOptimisticLockingException() throws InterruptedException {
		setUpElection();

		final int numThreads = 2;
		final CountDownLatch getLatch = new CountDownLatch(numThreads);
		final CountDownLatch saveLatch = new CountDownLatch(1);

		final ExecutorService executorService = Executors.newFixedThreadPool(numThreads, new CustomizableThreadFactory("lock-"));

		final int firstUpdateValue = 1;
		final CompletableFuture<?> updateFirst = CompletableFuture.runAsync(
				getTogetherThenUpdateFirst(getLatch, saveLatch),
				executorService);
		final CompletableFuture<?> updateSecond = CompletableFuture.runAsync(
				getTogetherThenUpdateSecond(getLatch, saveLatch),
				executorService);

		executorService.shutdown();
		final boolean normalTermination = executorService.awaitTermination(1, TimeUnit.SECONDS);
		assertTrue(normalTermination);

		assertFalse(updateFirst.isCompletedExceptionally());
		assertTrue(updateSecond.isCompletedExceptionally());

		final Optional<VerificationCardStateEntity> optionalVerificationCardState = verificationCardStateRepository.findByVerificationCardId(
				verificationCardId);
		assertTrue(optionalVerificationCardState.isPresent());
		assertEquals(firstUpdateValue, optionalVerificationCardState.get().getConfirmationAttempts());

		final Throwable cause = getCause(updateSecond);
		assertInstanceOf(OptimisticLockingFailureException.class, cause);
	}

	Runnable getTogetherThenUpdateFirst(final CountDownLatch checkLatch, final CountDownLatch saveLatch) {

		return () -> {
			final Optional<VerificationCardStateEntity> optionalVerificationCardState = verificationCardStateRepository.findByVerificationCardId(
					verificationCardId);
			if (optionalVerificationCardState.isPresent()) {
				final VerificationCardStateEntity verificationCardState = optionalVerificationCardState.get();
				checkLatch.countDown();

				awaitOneSecondWithRuntimeException(checkLatch);

				verificationCardState.setConfirmationAttempts(1);
				verificationCardStateRepository.save(verificationCardState);
				saveLatch.countDown();
			}
		};
	}

	Runnable getTogetherThenUpdateSecond(final CountDownLatch checkLatch, final CountDownLatch saveLatch) {
		return () -> {
			final Optional<VerificationCardStateEntity> optionalVerificationCardState = verificationCardStateRepository.findByVerificationCardId(
					verificationCardId);
			if (optionalVerificationCardState.isPresent()) {
				final VerificationCardStateEntity verificationCardState = optionalVerificationCardState.get();
				checkLatch.countDown();

				awaitOneSecondWithRuntimeException(checkLatch);
				awaitOneSecondWithRuntimeException(saveLatch);

				verificationCardState.setConfirmationAttempts(2);
				verificationCardStateRepository.save(verificationCardState);
			}
		};
	}

	private void setUpElection() {
		// Save election event.
		final ElectionEventEntity savedElectionEventEntity = electionEventService.save(electionEventId, encryptionGroup);

		// Save verification card set.
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity(verificationCardSetId,
				savedElectionEventEntity);
		verificationCardSetEntity.setAllowList(Collections.emptyList());
		verificationCardSetEntity.setCombinedCorrectnessInformation(new CombinedCorrectnessInformation(Collections.emptyList()));
		verificationCardSetService.save(verificationCardSetEntity);

		// Save verification card.
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(encryptionGroup);
		final ElGamalMultiRecipientPublicKey publicKey = elGamalGenerator.genRandomPublicKey(1);

		verificationCardService.save(new VerificationCard(verificationCardId, verificationCardSetId, publicKey));
	}

	private Throwable getCause(final CompletableFuture<?> future) throws InterruptedException {
		assert (future.isCompletedExceptionally());
		try {
			future.get();
		} catch (final ExecutionException e) {
			return e.getCause();
		}
		throw new IllegalStateException("Shouldn't reach this state.");
	}

	private void awaitOneSecondWithRuntimeException(final CountDownLatch countDownLatch) {
		awaitWithRuntimeException(countDownLatch, 1);
	}

	private void awaitWithRuntimeException(final CountDownLatch countDownLatch, int timeout) {
		final boolean awaited;
		try {
			awaited = countDownLatch.await(timeout, TimeUnit.SECONDS);
		} catch (final InterruptedException e) {
			throw new IllegalStateException("We should not reach this state.");
		}
		if (!awaited) {
			throw new IllegalStateException("Timeout for countDownLatch.");
		}
	}

}