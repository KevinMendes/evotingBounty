/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import javax.annotation.concurrent.ThreadSafe;

import com.rabbitmq.client.Connection;

/**
 * Connection manager.
 */
@ThreadSafe
interface ConnectionManager extends Destroyable {
	/**
	 * Returns a connection. Client should not close the returned connection.
	 *
	 * @return a connection
	 * @throws MessagingException failed to get a connection.
	 */
	Connection getConnection() throws MessagingException;
}
