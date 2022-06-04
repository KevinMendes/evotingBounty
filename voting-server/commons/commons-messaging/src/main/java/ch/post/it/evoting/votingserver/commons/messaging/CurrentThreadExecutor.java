/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.concurrent.Executor;

/**
 * Implementation of {@link Executor} which executes the commands in the current thread.
 */
class CurrentThreadExecutor implements Executor {
	private static final CurrentThreadExecutor INSTANCE = new CurrentThreadExecutor();

	private CurrentThreadExecutor() {
	}

	/**
	 * Returns the instance.
	 *
	 * @return the instance.
	 */
	public static CurrentThreadExecutor getInstance() {
		return INSTANCE;
	}

	@Override
	public void execute(Runnable command) {
		command.run();
	}
}
