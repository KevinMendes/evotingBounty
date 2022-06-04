/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.DoubleAdder;

public class JobProgressDetails {

	public static final JobProgressDetails EMPTY = new JobProgressDetails(new UUID(0, 0), -1L);

	private final UUID jobId;

	private final double totalWorkAmount;

	private final DoubleAdder workCompleted;
	private final Instant start;

	public JobProgressDetails(final UUID jobId, final long totalWorkAmount) {

		this.jobId = jobId;
		this.totalWorkAmount = totalWorkAmount;
		this.workCompleted = new DoubleAdder();
		this.start = Instant.now();

	}

	public UUID getJobId() {
		return jobId;
	}

	public double getTotalWorkAmount() {
		return totalWorkAmount;
	}

	public double getRemainingWork() {
		return totalWorkAmount - workCompleted.doubleValue();
	}

	public double getEstimatedTimeToCompletionInMillis() {
		Duration runningTime = Duration.between(start, Instant.now());
		final double currentWorkCompleted = workCompleted.doubleValue();
		if (currentWorkCompleted <= 0) {
			return -1L;
		}
		final double avgSpeed = runningTime.toMillis() / currentWorkCompleted;
		return avgSpeed * getRemainingWork();
	}

	@Override
	public String toString() {
		return String.format("%s : %f : %f ", jobId.toString(), getTotalWorkAmount(), getRemainingWork());
	}

	public void incrementWorkCompleted(final double amountCompleted) {
		workCompleted.add(amountCompleted);
	}
}
