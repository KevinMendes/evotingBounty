/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import com.github.benmanes.caffeine.cache.Cache;

@SpringBootTest(properties = { "orchestrator.request.cache.timeout.seconds=1" })
@ContextConfiguration(classes = { MessageBrokerOrchestratorApplicationConfig.class })
class InFlightRequestsCacheTest {

	@Autowired
	private Cache<String, CompletableFuture<String>> inFlightRequests;

	@RepeatedTest(10)
	void cacheEntryExpiresAfterCreation() {
		final String key = "Test";
		inFlightRequests.put(key, new CompletableFuture<>());
		final CompletableFuture<String> present = inFlightRequests.getIfPresent(key);
		assertNotNull(present);
		assertDoesNotThrow(
				() -> await()
						.atMost(2, TimeUnit.SECONDS)
						.until(() -> inFlightRequests.getIfPresent(key) == null)
		);
	}

}