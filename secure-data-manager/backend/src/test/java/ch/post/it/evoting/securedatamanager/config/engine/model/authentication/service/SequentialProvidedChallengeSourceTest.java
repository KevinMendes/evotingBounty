/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ProvidedChallenges;

class SequentialProvidedChallengeSourceTest {

	@Test
	void returnCorrectNextValue() throws URISyntaxException {
		final URL url = this.getClass().getResource("/aliasDataSample.csv");
		final Path aliasesPath = new File(url.toURI()).toPath();
		final SequentialProvidedChallengeSource source = new SequentialProvidedChallengeSource(aliasesPath);
		final ProvidedChallenges challenges = source.next();
		final String alias = challenges.getAlias();
		assertEquals("alias1", alias);
	}

	@Test
	void returnCorrectNextValuesForThreads() throws URISyntaxException, InterruptedException {
		final URL url = this.getClass().getResource("/aliasDataSample.csv");
		final Path aliasesPath = new File(url.toURI()).toPath();
		final SequentialProvidedChallengeSource source = new SequentialProvidedChallengeSource(aliasesPath);

		final ExecutorService executor = Executors.newFixedThreadPool(10);
		final CountDownLatch latch = new CountDownLatch(10);

		final Queue<String> queue = new ConcurrentLinkedQueue<>();
		for (int i = 0; i < 10; i++) {
			executor.execute(() -> {
				queue.add(source.next().getAlias());
				latch.countDown();
			});
		}

		latch.await();

		assertTrue(queue.contains("alias1"));
		assertTrue(queue.contains("alias2"));
		assertTrue(queue.contains("alias3"));
		assertTrue(queue.contains("alias4"));
		assertTrue(queue.contains("alias5"));
		assertTrue(queue.contains("alias6"));
		assertTrue(queue.contains("alias7"));
		assertTrue(queue.contains("alias8"));
		assertTrue(queue.contains("alias9"));
		assertTrue(queue.contains("alias10"));
	}
}
