/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Factory of {@link Sender}.
 */
@ThreadSafe
interface SenderFactory {
	/**
	 * Creates a new sender.
	 *
	 * @return a new sender
	 * @throws MessagingException failed to create a sender.
	 */
	Sender newSender() throws MessagingException;
}
