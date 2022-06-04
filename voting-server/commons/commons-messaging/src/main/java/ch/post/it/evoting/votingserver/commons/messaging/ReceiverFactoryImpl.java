/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import java.io.IOException;
import java.util.concurrent.Executor;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Implementation of {@link ReceiverFactory}.
 */
class ReceiverFactoryImpl implements ReceiverFactory {
	private final ConnectionManager connectionManager;

	private final Codec codec;

	/**
	 * Constructor.
	 *
	 * @param connectionManager
	 * @param codec
	 * @param executor
	 */
	public ReceiverFactoryImpl(ConnectionManager connectionManager, Codec codec) {
		this.connectionManager = connectionManager;
		this.codec = codec;
	}

	@Override
	public Receiver newReceiver(Destination destination, MessageListener listener, Executor executor) throws MessagingException {
		Connection connection = connectionManager.getConnection();
		Channel channel;
		try {
			channel = connection.createChannel();
		} catch (IOException e) {
			throw new MessagingException("Failed to create receiver.", e);
		}
		return new ReceiverImpl(channel, destination, listener, executor, codec);
	}
}
