/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Tests of {@link ConnectionManagerImpl}.
 */
public class ConnectionManagerImplTest {
	private Connection connection;

	private ConnectionFactory factory;

	private ConnectionManagerImpl manager;

	@Before
	public void setUp() throws IOException, TimeoutException {
		connection = mock(Connection.class);

		factory = mock(ConnectionFactory.class);
		when(factory.newConnection()).thenReturn(connection);

		manager = new ConnectionManagerImpl(factory);
	}

	@Test
	public void testDestroyHavingConnection() throws MessagingException, IOException {
		manager.getConnection();
		manager.destroy();
		verify(connection).close();
	}

	@Test(expected = MessagingException.class)
	public void testDestroyIOException() throws IOException, MessagingException {
		doThrow(new IOException("test")).when(connection).close();
		manager.getConnection();
		manager.destroy();
	}

	@Test
	public void testDestroyNoConnection() throws MessagingException {
		manager.destroy();
	}

	@Test
	public void testGetConnection() throws MessagingException {
		assertEquals(connection, manager.getConnection());
		assertEquals(connection, manager.getConnection());
	}

	@Test(expected = MessagingException.class)
	public void testGetConnectionDestroyed() throws MessagingException {
		manager.destroy();
		manager.getConnection();
	}

	@Test(expected = MessagingException.class)
	public void testGetConnectionIOException() throws IOException, TimeoutException, MessagingException {
		when(factory.newConnection()).thenThrow(new IOException("test"));
		manager.getConnection();
	}

	@Test(expected = MessagingException.class)
	public void testGetConnectionTimeoutException() throws IOException, TimeoutException, MessagingException {
		when(factory.newConnection()).thenThrow(new TimeoutException("test"));
		manager.getConnection();
	}
}
