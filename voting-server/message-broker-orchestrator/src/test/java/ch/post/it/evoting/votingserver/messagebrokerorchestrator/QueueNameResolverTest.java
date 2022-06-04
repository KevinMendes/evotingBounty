/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_RESPONSE_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class QueueNameResolverTest {

	private final QueueNameResolver queueNameResolver = new QueueNameResolver();

	@Test
	void testGet() {
		final String[] lcc = queueNameResolver.get("CREATE_LCC_SHARE_RESPONSE_PATTERN");

		assertEquals(MessageBrokerOrchestratorApplication.NODE_IDS.size(), lcc.length);
		Arrays.stream(lcc).forEach(q -> {
			assertTrue(q.startsWith(CREATE_LCC_SHARE_RESPONSE_PATTERN));
			assertTrue(MessageBrokerOrchestratorApplication.NODE_IDS.contains(Integer.parseInt(q.substring(q.lastIndexOf('.') + 1))));
		});
	}
}
