/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Tests of the {@link CurrentThreadExecutor}:
 */
public class CurrentThreadExecutorTest {
	@Test
	public void testExecute() {
		AtomicReference<Thread> reference = new AtomicReference<Thread>();
		Runnable command = () -> reference.set(Thread.currentThread());
		CurrentThreadExecutor.getInstance().execute(command);
		assertEquals(reference.get(), Thread.currentThread());
	}
}
