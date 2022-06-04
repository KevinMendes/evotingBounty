/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

class RetriableSharesTaskTest {

	@Test
	void succeedAtFirstTry() {
		assertTimeout(Duration.ofMillis(30), () -> new RetriableSharesTask<>(new TestCallable()).call());
	}

	@Test
	void throwsNonTokenException() {
		final RetriableSharesTask<Void> retriableSharesTask = new RetriableSharesTask<>(() -> {
			throw new IllegalStateException();
		});

		final SharesRuntimeException sharesRuntimeException = assertThrows(SharesRuntimeException.class, retriableSharesTask::call);

		assertTrue(sharesRuntimeException.getCause() instanceof IllegalStateException);
	}

	@Test
	void succeedsAtSecondTry() {
		assertTimeout(Duration.ofMillis(RetriableSharesTask.DEFAULT_WAIT_TIME + 30), () -> new RetriableSharesTask<>(new TestCallable(2)).call());
	}

	@Test
	void tryOutBeforeSuccess() {
		final RetriableSharesTask<Void> retriableSharesTask = new RetriableSharesTask<>(2, 500L, new TestCallable(3));

		final SharesRuntimeException sharesRuntimeException = assertThrows(SharesRuntimeException.class, retriableSharesTask::call);

		assertAll(() -> assertTrue(sharesRuntimeException.getMessage().contains("2 attempts")),
				() -> assertTrue(sharesRuntimeException.getMessage().contains("500ms")));
	}

	@Test
	void interruptRetriableTask() {
		// assert the test run took less than the default 1000ms, so interruption affected runtime.
		assertTimeout(Duration.ofMillis(500), () -> {
			final Runnable task = () -> new RetriableSharesTask<>(new TestCallable(2)).call();
			final Thread t = new Thread(task);
			t.start();

			// wait for the task to be in the pause between retries
			Awaitility.await().until(() -> Thread.State.TIMED_WAITING.equals(t.getState()));

			t.interrupt();
		});

	}

}
