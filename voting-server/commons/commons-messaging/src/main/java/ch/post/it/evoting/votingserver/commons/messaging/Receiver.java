/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * Receiver.
 */
interface Receiver extends Destroyable {
	/**
	 * Receives messages.
	 *
	 * @throws DestinationNotFoundException destination does not exist
	 * @throws MessagingException           failed to receive messages.
	 */
	void receive() throws MessagingException;
}
