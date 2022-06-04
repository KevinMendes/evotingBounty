/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.progress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;

@ExtendWith(MockitoExtension.class)
class ProgressManagerTest {

	@InjectMocks
	ProgressManager sut = new ProgressManager();

	@Test
	void returnEmptyIfJobNotRegistered() {

		final Optional<JobProgressDetails> progress = sut.getJobProgress(UUID.randomUUID());
		assertFalse(progress.isPresent());
	}

	@Test
	void returnProgressIfJobRegistered() {

		final UUID jobId = UUID.randomUUID();
		sut.registerJob(jobId, JobProgressDetails.EMPTY);

		final Optional<JobProgressDetails> progress = sut.getJobProgress(jobId);

		assertTrue(progress.isPresent());
	}

	@Test
	void removeEmptyAfterUnregisterJob() {

		final UUID jobId = UUID.randomUUID();
		sut.registerJob(jobId, JobProgressDetails.EMPTY);
		final Optional<JobProgressDetails> progress = sut.getJobProgress(jobId);
		assertTrue(progress.isPresent());

		sut.unregisterJob(jobId);

		final Optional<JobProgressDetails> progress2 = sut.getJobProgress(jobId);
		assertFalse(progress2.isPresent());

	}

	@Test
	void returnUpdatedWorkCompletedAfterUpdateJob() {

		final UUID jobId = UUID.randomUUID();
		final long totalWorkAmount = 1;
		final JobProgressDetails progressDetails = new JobProgressDetails(jobId, totalWorkAmount);
		sut.registerJob(jobId, progressDetails);
		final Optional<JobProgressDetails> progress = sut.getJobProgress(jobId);
		assertTrue(progress.isPresent());
		assertEquals(totalWorkAmount, (long) progress.get().getTotalWorkAmount());

		final long workCompleted = 1;
		sut.updateProgress(jobId, workCompleted);

		final Optional<JobProgressDetails> updatedProgress = sut.getJobProgress(jobId);
		assertTrue(updatedProgress.isPresent());
		assertEquals(0, (long) progress.get().getRemainingWork());

	}
}
