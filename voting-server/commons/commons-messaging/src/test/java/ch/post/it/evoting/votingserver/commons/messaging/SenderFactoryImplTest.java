/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * Tests of {@link SenderFactoryImpl}.
 */
public class SenderFactoryImplTest {
	private Channel channel;

	private Connection connection;

	private ConnectionManager manager;

	private SenderFactoryImpl factory;

	@Before
	public void setUp() throws MessagingException, IOException {
		channel = mock(Channel.class);

		connection = mock(Connection.class);
		when(connection.createChannel()).thenReturn(channel);

		manager = mock(ConnectionManager.class);
		when(manager.getConnection()).thenReturn(connection);

		factory = new SenderFactoryImpl(manager, CodecImpl.getInstance());
	}

	@Test
	public void testNewSender() throws MessagingException, IOException, TimeoutException {
		Sender sender = factory.newSender();
		sender.destroy();
		verify(channel).close();
	}

	@Test(expected = MessagingException.class)
	public void testNewSenderErrorInConnection() throws MessagingException, IOException {
		when(connection.createChannel()).thenThrow(new IOException("test"));
		factory.newSender();
	}

	@Test(expected = MessagingException.class)
	public void testNewSenderErrorInManager() throws MessagingException {
		when(manager.getConnection()).thenThrow(new MessagingException("test"));
		factory.newSender();
	}
}
