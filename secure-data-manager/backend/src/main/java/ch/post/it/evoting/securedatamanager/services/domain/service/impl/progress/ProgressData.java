/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress;

import java.util.concurrent.CompletableFuture;

public class ProgressData<T> {
	private final CompletableFuture<T> future;
	private T progressStatus;

	ProgressData(final CompletableFuture<T> future, final T progressStatus) {
		this.future = future;
		this.progressStatus = progressStatus;
	}

	public CompletableFuture<T> getFuture() {
		return future;
	}

	public T getProgressStatus() {
		return progressStatus;
	}

	public void setProgressStatus(final T status) {
		this.progressStatus = status;
	}
}
