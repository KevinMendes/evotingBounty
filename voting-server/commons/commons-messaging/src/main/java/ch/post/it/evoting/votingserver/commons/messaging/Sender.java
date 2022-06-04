/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

/**
 * Sender.
 */
interface Sender extends Destroyable {
	/**
	 * Returns whether the sender is valid.
	 *
	 * @return the sender is valid.
	 */
	boolean isValid();

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
}
