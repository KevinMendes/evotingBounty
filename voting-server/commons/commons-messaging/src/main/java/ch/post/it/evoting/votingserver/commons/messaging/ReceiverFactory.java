/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory for {@link Receiver}:
 */
@ThreadSafe
interface ReceiverFactory {
	/**
	 * Creates a new receiver for given destination, listener and executor.
	 *
	 * @param destination the destination
	 * @param listener    the listener
	 * @return a new receiver
	 * @throws MessagingException failed to create a receiver.
	 */
	Receiver newReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException;
}
