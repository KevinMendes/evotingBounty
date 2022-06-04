/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.errormanagement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExponentialBackoffExecutionPolicyTest {
	@Test
	public void should_not_fail_if_managed_function_does_not_fail() throws Throwable {
		ExponentialBackoffExecutionPolicy sut = new ExponentialBackoffExecutionPolicy(1, 1);

		// Do nothing.
		sut.execute(() -> null);
	}

	@Test
	public void should_not_fail_if_managed_function_works_in_time() throws Throwable {
		final int allowedAttempts = 3;
		ExponentialBackoffExecutionPolicy sut = new ExponentialBackoffExecutionPolicy(allowedAttempts, 1);

		HardToGet hardToGet = new HardToGet(allowedAttempts);
		int attempts = sut.execute(hardToGet::play);

		assertEquals(allowedAttempts, attempts);
	}

	@Test(expected = OutOfRetriesException.class)
	public void should_fail_if_managed_function_takes_more_than_the_allowed_attempts() throws Throwable {
		final int allowedAttempts = 3;
		ExponentialBackoffExecutionPolicy sut = new ExponentialBackoffExecutionPolicy(allowedAttempts, 1);

		HardToGet hardToGet = new HardToGet(allowedAttempts + 1);
		sut.execute(hardToGet::play);
	}

	/**
	 * Class with a method that takes a few attempts to work.
	 */
	private static class HardToGet {
		private final int failCount;

		private int attempts = 0;

		public HardToGet(int failCount) {
			this.failCount = failCount;
		}

		private int play() {
			attempts++;

			if (attempts < failCount) {
				throw new RuntimeException("Nope");
			}

			return attempts;
		}
	}
}
