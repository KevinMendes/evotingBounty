/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Sender manager.
 */
@ThreadSafe
interface SenderManager extends Destroyable {
	/**
	 * Acquires a sender.
	 *
	 * @return a sender
	 * @throws MessagingException failed to acquire a sender.
	 */
	Sender acquireSender() throws MessagingException;

	/**
	 * Releases a sender.
	 *
	 * @param sender the sender
	 * @throws MessagingException failed to release the sender.
	 */
	void releaseSender(Sender sender) throws MessagingException;
}
