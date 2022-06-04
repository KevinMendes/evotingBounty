/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

/**
 * Messaging service.
 */
public interface MessagingService {
	/**
	 * <p>
	 * Creates a receiver for given destination and listener. This is a shortcut for
	 *
	 * <pre>
	 * <code>
	 * createReceiver(Destination destination, listener, c -> c.run());
	 * </code>
	 * </pre>
	 *
	 * @param destination the destination
	 * @param listener    the listener
	 * @throws DestinationNotFoundException the destination does not exist
	 * @throws MessagingException           failed to create a receiver.
	 */
	void createReceiver(Destination destination, MessageListener listener) throws MessagingException;

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
	void createReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException;

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

	/**
	 * <p>
	 * Send a given message to the specified destination. The message must be one of the following:
	 * <ul>
	 * <li>{@code byte[]}</li>
	 * <li>{@link StreamSerializable}</li>
	 * </ul>
	 * otherwise {@link InvalidMessageException} is thrown.
	 *
	 * @param destination the destination
	 * @param message     the message
	 * @throws DestinationNotFoundException the destination does not exist
	 * @throws InvalidMessageException      the message is invalid
	 * @throws MessagingException           failed to send the message
	 */
	void send(Destination destination, Object message) throws MessagingException;

	/**
	 * Shuts the service down. This method automatically destroys all the receiver created by {@link #createReceiver(Destination, MessageListener)}
	 * method. is already shut down then no operation.
	 */
	void shutdown();
}
