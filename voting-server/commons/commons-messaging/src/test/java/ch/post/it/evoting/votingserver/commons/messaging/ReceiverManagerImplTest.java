/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link ReceiverManagerImpl}.
 */
public class ReceiverManagerImplTest {
	private static final Destination DESTINATION1 = new Queue("queue");

	private static final Destination DESTINATION2 = new Topic("topic");

	private static final MessageListener LISTENER1 = mock(MessageListener.class);

	private static final MessageListener LISTENER2 = mock(MessageListener.class);

	private static final CurrentThreadExecutor EXECUTOR = CurrentThreadExecutor.getInstance();

	private Receiver receiver;

	private ReceiverFactory factory;

	private ReceiverManagerImpl manager;

	@Before
	public void setUp() throws MessagingException {
		receiver = mock(Receiver.class);

		factory = mock(ReceiverFactory.class);
		when(factory.newReceiver(DESTINATION1, LISTENER1, EXECUTOR)).thenReturn(receiver);

		manager = new ReceiverManagerImpl(factory);
	}

	@Test
	public void testCreateReceiver() throws MessagingException {
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		verify(receiver).receive();
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testCreateReceiverDestinationNotFound() throws MessagingException {
		doThrow(new DestinationNotFoundException("test")).when(receiver).receive();
		try {
			manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		} finally {
			verify(receiver).destroy();
		}
	}

	@Test(expected = MessagingException.class)
	public void testCreateReceiverDestroyed() throws MessagingException {
		manager.destroy();
		try {
			manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		} finally {
			verify(receiver, never()).receive();
		}
	}

	@Test(expected = MessagingException.class)
	public void testCreateReceiverErrorInFactory() throws MessagingException {
		when(factory.newReceiver(DESTINATION1, LISTENER1, EXECUTOR)).thenThrow(new MessagingException("test"));
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
	}

	@Test(expected = MessagingException.class)
	public void testCreateReceiverErrorInReceiver() throws MessagingException {
		doThrow(new MessagingException("test")).when(receiver).receive();
		try {
			manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		} finally {
			verify(receiver).destroy();
		}
	}

	@Test
	public void testDestroyHasReceivers() throws MessagingException {
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		manager.destroy();
		verify(receiver).destroy();
	}

	@Test
	public void testDestroyNoReceivers() throws MessagingException {
		manager.destroy();
	}

	@Test
	public void testDestroyReceiver() throws MessagingException {
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		manager.destroyReceiver(DESTINATION1, LISTENER1);
		verify(receiver).destroy();
	}

	@Test
	public void testDestroyReceiverDifferentDestination() throws MessagingException {
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		manager.destroyReceiver(DESTINATION2, LISTENER1);
		verify(receiver, never()).destroy();
	}

	@Test
	public void testDestroyReceiverDifferentListener() throws MessagingException {
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		manager.destroyReceiver(DESTINATION1, LISTENER2);
		verify(receiver, never()).destroy();
	}

	@Test(expected = MessagingException.class)
	public void testDestroyReceiverError() throws MessagingException {
		doThrow(new MessagingException("test")).when(receiver).destroy();
		manager.createReceiver(DESTINATION1, LISTENER1, EXECUTOR);
		manager.destroyReceiver(DESTINATION1, LISTENER1);
	}
}
