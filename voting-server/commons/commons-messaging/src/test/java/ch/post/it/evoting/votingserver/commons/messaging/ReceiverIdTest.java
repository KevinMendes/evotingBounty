/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;

/**
 * Tests of {@link ReceiverId}.
 */
public class ReceiverIdTest {
	@Test
	public void testEqualsObject() {
		Destination queue1 = new Queue("queue");
		Destination queue2 = new Queue("queue");
		Destination queue3 = new Queue("anotherQueue");

		MessageListener listener1 = mock(MessageListener.class);
		MessageListener listener2 = mock(MessageListener.class);

		ReceiverId id1 = new ReceiverId(queue1, listener1);
		ReceiverId id2 = new ReceiverId(queue2, listener1);
		ReceiverId id3 = new ReceiverId(queue3, listener1);
		ReceiverId id4 = new ReceiverId(queue1, listener2);

		assertEquals(id1, id2);
		assertNotEquals(id1, id3);
		assertNotEquals(id1, id4);
	}
}
