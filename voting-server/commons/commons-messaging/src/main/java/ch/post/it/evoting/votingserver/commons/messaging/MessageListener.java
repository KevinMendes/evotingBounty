/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * <p>
 * Message listener.
 * <p>
 * The concrete message type in the {@link #onMessage(Object)} method can be one of the types listed in the contract of {@link
 * MessagingService#send(Destination, Object)}.
 * <p>
 * An instance of this interface can be called concurrently if it is used by more than one receiver or the executor passed to {@link
 * MessagingService#createReceiver(Destination, MessageListener, java.util.concurrent.Executor)} allows parallel execution.
 */
@FunctionalInterface
public interface MessageListener {
	/**
	 * <p>
	 * Message received.
	 *
	 * @param message the message.
	 */
	void onMessage(Object message);
}
