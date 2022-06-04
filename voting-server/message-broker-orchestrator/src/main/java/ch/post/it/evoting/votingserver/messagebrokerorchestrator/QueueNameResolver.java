/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import org.springframework.stereotype.Component;

import ch.post.it.evoting.domain.SharedQueue;

@Component
public class QueueNameResolver {

	public String[] get(final String name) {
		return MessageBrokerOrchestratorApplication.NODE_IDS.stream()
				.map(nodeId -> String.format("%s%s", SharedQueue.fromName(name), nodeId))
				.toArray(String[]::new);
	}
}
