/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Receiver manager.
 */
@ThreadSafe
interface ReceiverManager extends Destroyable {

	/**
	 * <p>
	 * Creates a receiver for given destination and listener. The specified executor is used to perform {@link Consumer#accept(Object)} calls.
	 * <p>
	 * The internally created receiver is identified by destination (using equals) and listener (using object identity). If such a receiver already
	 * exists then no operation.
	 * <p>
	 * The listener must be prepared to receive messages before this method returns.
	 *
	 * @param destination the destination
	 * @param listener    the listener
	 * @throws DestinationNotFoundException the destination does not exist
	 * @throws MessagingException           failed to create a receiver.
	 */
	<T> void createReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException;

	/**
	 * <p>
	 * Destroys the existing receiver identified by given destination (using equals) and listener (using object identity). If such a receiver does not
	 * exist then no operation.
	 * <p>
	 * The listener must be prepared to receive a few messages even after this method returns.
	 *
	 * @param destination the destination
	 * @param listener    the listener
	 * @throws MessagingException failed to destor the receiver.
	 */
	void destroyReceiver(Destination destination, MessageListener listener) throws MessagingException;

}
