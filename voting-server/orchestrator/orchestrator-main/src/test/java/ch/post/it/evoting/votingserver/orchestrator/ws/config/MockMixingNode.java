/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Queue;

/**
 * Simulates a mix/decrypt control component node.
 */
public class MockMixingNode implements AutoCloseable {
	private static final Logger logger = LoggerFactory.getLogger(MockMixingNode.class);

	private final MessagingService messagingService;

	private final Queue requestQueue;

	private final MessageListener listener;

	/**
	 * @param messagingService  an instance of the messaging service to be able to send response messages
	 * @param nodeName          the name of the mocked node
	 * @param requestQueueName  the name of the queue the node listens to
	 * @param responseQueueName the name of the queue the node responds to
	 */
	public MockMixingNode(MessagingService messagingService, String nodeName, String requestQueueName, String responseQueueName,
			MessageListener listener) throws MessagingException {
		this.messagingService = messagingService;
		this.requestQueue = new Queue(requestQueueName);
		this.listener = listener;

		// Start listening to the request queue.
		messagingService.createReceiver(requestQueue, listener);
	}

	@Override
	public void close() {
		try {
			messagingService.destroyReceiver(requestQueue, listener);
		} catch (MessagingException e) {
			logger.error("The mock messaging listener failed to destroy its receiver for queue {}", requestQueue);
		}
	}
}
