/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.common;

public class JobProgressDetails {

	public static final JobProgressDetails UNKNOWN = new JobProgressDetails();

	private int remainingWork = -1;
	private int totalWorkAmount = -1;
	private long estimatedTimeToCompletionInMillis = -1;

	public int getRemainingWork() {
		return remainingWork;
	}

	protected void setRemainingWork(final int remainingWork) {
		this.remainingWork = remainingWork;
	}

	public int getTotalWorkAmount() {
		return totalWorkAmount;
	}

	protected void setTotalWorkAmount(final int totalWorkAmount) {
		this.totalWorkAmount = totalWorkAmount;
	}

	public long getEstimatedTimeToCompletionInMillis() {
		return estimatedTimeToCompletionInMillis;
	}

	protected void setEstimatedTimeToCompletionInMillis(final long estimatedTimeToCompletionInMillis) {
		this.estimatedTimeToCompletionInMillis = estimatedTimeToCompletionInMillis;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("{");
		sb.append("remainingWork=").append(remainingWork);
		sb.append(", totalWorkAmount=").append(totalWorkAmount);
		sb.append(", estimatedTimeToCompletionInMillis=").append(estimatedTimeToCompletionInMillis);
		sb.append('}');
		return sb.toString();
	}
}
