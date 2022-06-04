/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Implementation of {@link ConnectionManager}.
 */
class ConnectionManagerImpl implements ConnectionManager {
	private final ConnectionFactory factory;

	private Connection connection;

	private boolean destroyed;

	/**
	 * Constructor.
	 *
	 * @param factory
	 */
	public ConnectionManagerImpl(ConnectionFactory factory) {
		this.factory = factory;
	}

	@Override
	public synchronized void destroy() throws MessagingException {
		if (!destroyed) {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
					throw new MessagingException("Failed to destroy connection manager.", e);
				}
			}
			destroyed = true;
		}
	}

	@Override
	public synchronized Connection getConnection() throws MessagingException {
		if (destroyed) {
			throw new MessagingException("Connection manager is already destroyed.");
		}
		if (connection == null) {
			try {
				connection = factory.newConnection();
			} catch (IOException | TimeoutException e) {
				throw new MessagingException("Failed to create a connection.", e);
			}
		}
		return connection;
	}
}
