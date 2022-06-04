/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Implementation of {@link SenderFactory}.
 */
class SenderFactoryImpl implements SenderFactory {
	private final ConnectionManager connectionManager;

	private final Codec codec;

	/**
	 * Constructor.
	 *
	 * @param connectionManager
	 * @param codec
	 */
	public SenderFactoryImpl(ConnectionManager connectionManager, Codec codec) {
		this.connectionManager = connectionManager;
		this.codec = codec;
	}

	@Override
	public Sender newSender() throws MessagingException {
		return new SenderImpl(getChannel(), codec);
	}

	private Channel getChannel() throws MessagingException {
		Connection connection = connectionManager.getConnection();

		try {
			return connection.createChannel();
		} catch (IOException e) {
			throw new MessagingException("Failed to create sender.", e);
		}
	}
}
