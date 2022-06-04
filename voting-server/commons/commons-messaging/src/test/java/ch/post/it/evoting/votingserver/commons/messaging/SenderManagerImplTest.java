/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link SenderManagerImpl}.
 */
public class SenderManagerImplTest {
	private Sender sender1;

	private Sender sender2;

	private SenderFactory factory;

	private SenderManagerImpl manager;

	@Before
	public void setUp() throws Exception {
		sender1 = mock(Sender.class);
		when(sender1.isValid()).thenReturn(true);
		sender2 = mock(Sender.class);
		when(sender1.isValid()).thenReturn(true);

		factory = mock(SenderFactory.class);
		when(factory.newSender()).thenReturn(sender1, sender2);

		manager = new SenderManagerImpl(factory, 1);
	}

	@Test
	public void testAcquireSender() throws MessagingException {
		assertEquals(sender1, manager.acquireSender());
		assertEquals(sender2, manager.acquireSender());
	}

	@Test(expected = MessagingException.class)
	public void testAcquireSenderDestroyed() throws MessagingException {
		manager.destroy();
		manager.acquireSender();
	}

	@Test(expected = MessagingException.class)
	public void testAcquireSenderError() throws MessagingException {
		when(factory.newSender()).thenThrow(new MessagingException("test"));
		manager.acquireSender();
	}

	@Test
	public void testDestroy() throws MessagingException {
		manager.acquireSender();
		manager.acquireSender();
		manager.destroy();
		verify(sender1).destroy();
		verify(sender2).destroy();
	}

	@Test(expected = MessagingException.class)
	public void testDestroyError() throws MessagingException {
		manager.acquireSender();
		doThrow(new MessagingException("test")).when(sender1).destroy();
		manager.destroy();
	}

	@Test(expected = MessagingException.class)
	public void testReleaseSenderError() throws MessagingException {
		doThrow(new MessagingException("test")).when(sender2).destroy();
		manager.acquireSender();
		manager.releaseSender(manager.acquireSender());
	}

	@Test
	public void testReleaseSenderExtra() throws MessagingException {
		manager.acquireSender();
		manager.releaseSender(manager.acquireSender());
		verify(sender2).destroy();
	}

	@Test
	public void testReleaseSenderInvalid() throws MessagingException {
		when(sender1.isValid()).thenReturn(false);
		manager.releaseSender(manager.acquireSender());
		assertEquals(sender2, manager.acquireSender());
	}

	@Test
	public void testReleaseSenderReused() throws MessagingException {
		manager.releaseSender(manager.acquireSender());
		assertEquals(sender1, manager.acquireSender());
	}
}
