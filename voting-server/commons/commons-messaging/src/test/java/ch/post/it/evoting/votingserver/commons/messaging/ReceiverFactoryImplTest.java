/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Tests of {@link ReceiverFactoryImpl}.
 */
public class ReceiverFactoryImplTest {
	private static final Destination DESTINATION = new Queue("queue");

	private static final MessageListener LISTENER = mock(MessageListener.class);

	private static final Executor EXECUTOR = mock(Executor.class);

	private Channel channel;

	private Connection connection;

	private ConnectionManager connectionManager;

	private ReceiverFactoryImpl factory;

	@Before
	public void setUp() throws IOException, MessagingException {
		channel = mock(Channel.class);

		connection = mock(Connection.class);
		when(connection.createChannel()).thenReturn(channel);

		connectionManager = mock(ConnectionManager.class);
		when(connectionManager.getConnection()).thenReturn(connection);

		factory = new ReceiverFactoryImpl(connectionManager, CodecImpl.getInstance());
	}

	@Test
	public void testNewReceiver() throws MessagingException {
		ReceiverImpl receiver = (ReceiverImpl) factory.newReceiver(DESTINATION, LISTENER, EXECUTOR);
		assertEquals(channel, receiver.getChannel());
	}

	@Test(expected = MessagingException.class)
	public void testNewReceiverErrorInConnectionManager() throws MessagingException {
		when(connectionManager.getConnection()).thenThrow(new MessagingException("test"));
		factory.newReceiver(DESTINATION, LISTENER, EXECUTOR);
	}

	@Test(expected = MessagingException.class)
	public void testNewReceiverIOExceptionInConnection() throws IOException, MessagingException {
		when(connection.createChannel()).thenThrow(new IOException("test"));
		factory.newReceiver(DESTINATION, LISTENER, EXECUTOR);
	}
}
