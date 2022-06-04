/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link MessagingServiceImpl}.
 */
public class MessagingServiceImplTest {
	private static final Destination DESTINATION = new Queue("queue");

	private static final MessageListener LISTENER = mock(MessageListener.class);

	private static final byte[] MESSAGE = { 1, 2, 3 };

	private ConnectionManager connectionManager;

	private Sender sender;

	private SenderManager senderManager;

	private ReceiverManager receiverManager;

	private MessagingServiceImpl service;

	@Before
	public void setUp() throws MessagingException {
		connectionManager = mock(ConnectionManager.class);

		sender = mock(Sender.class);
		senderManager = mock(SenderManager.class);
		when(senderManager.acquireSender()).thenReturn(sender);

		receiverManager = mock(ReceiverManager.class);

		service = new MessagingServiceImpl(connectionManager, senderManager, receiverManager);
	}

	@Test
	public void testCreateReceiverDestinationMessageListener() throws MessagingException {
		service.createReceiver(DESTINATION, LISTENER);
		verify(receiverManager).createReceiver(DESTINATION, LISTENER, CurrentThreadExecutor.getInstance());
	}

	@Test
	public void testCreateReceiverDestinationMessageListenerExecutor() throws MessagingException {
		Executor executor = mock(Executor.class);
		service.createReceiver(DESTINATION, LISTENER, executor);
		verify(receiverManager).createReceiver(DESTINATION, LISTENER, executor);
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testCreateReceiverDestinationMessageListenerExecutorDestinationNotFound() throws MessagingException {
		Executor executor = mock(Executor.class);
		doThrow(new DestinationNotFoundException("test")).when(receiverManager).createReceiver(DESTINATION, LISTENER, executor);
		service.createReceiver(DESTINATION, LISTENER, executor);
	}

	@Test(expected = MessagingException.class)
	public void testCreateReceiverDestinationMessageListenerExecutorError() throws MessagingException {
		Executor executor = mock(Executor.class);
		doThrow(new MessagingException("test")).when(receiverManager).createReceiver(DESTINATION, LISTENER, executor);
		service.createReceiver(DESTINATION, LISTENER, executor);
	}

	@Test
	public void testDestroyReceiver() throws MessagingException {
		service.destroyReceiver(DESTINATION, LISTENER);
		verify(receiverManager).destroyReceiver(DESTINATION, LISTENER);
	}

	@Test(expected = MessagingException.class)
	public void testDestroyReceiverException() throws MessagingException {
		doThrow(new MessagingException("test")).when(receiverManager).destroyReceiver(DESTINATION, LISTENER);
		service.destroyReceiver(DESTINATION, LISTENER);
	}

	@Test
	public void testSend() throws MessagingException {
		service.send(DESTINATION, MESSAGE);
		verify(sender).send(DESTINATION, MESSAGE);
		verify(senderManager).releaseSender(sender);
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testSendDestinationNotFound() throws MessagingException {
		doThrow(new DestinationNotFoundException("test")).when(sender).send(DESTINATION, MESSAGE);
		try {
			service.send(DESTINATION, MESSAGE);
		} finally {
			verify(senderManager).releaseSender(sender);
		}
	}

	@Test(expected = MessagingException.class)
	public void testSendErrorInSender() throws MessagingException {
		doThrow(new MessagingException("test")).when(sender).send(DESTINATION, MESSAGE);
		try {
			service.send(DESTINATION, MESSAGE);
		} finally {
			verify(senderManager).releaseSender(sender);
		}
	}

	@Test(expected = MessagingException.class)
	public void testSendErrorInSenderManager() throws MessagingException {
		when(senderManager.acquireSender()).thenThrow(new MessagingException("test"));
		service.send(DESTINATION, MESSAGE);
	}

	@Test(expected = InvalidMessageException.class)
	public void testSendInvalidMessage() throws MessagingException {
		doThrow(new InvalidMessageException("test")).when(sender).send(DESTINATION, MESSAGE);
		try {
			service.send(DESTINATION, MESSAGE);
		} finally {
			verify(senderManager).releaseSender(sender);
		}
	}

	@Test
	public void testShutdown() throws MessagingException {
		service.shutdown();
		verify(connectionManager).destroy();
		verify(senderManager).destroy();
		verify(receiverManager).destroy();
	}

	@Test
	public void testShutdownException() throws MessagingException {
		doThrow(new MessagingException("test")).when(connectionManager).destroy();
		service.shutdown();
	}
}
