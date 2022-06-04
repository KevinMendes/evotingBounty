/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests of {@link Destinations}.
 */
public class DestinationsTest {

	@Test
	public void testGetExchangeQueue() {
		assertEquals("", Destinations.getExchange(new Queue("queue")));
	}

	@Test
	public void testGetExchangeTopic() {
		assertEquals("topic", Destinations.getExchange(new Topic("topic")));
	}

	@Test
	public void testGetRoutingKeyQueue() {
		assertEquals("queue", Destinations.getRoutingKey(new Queue("queue")));
	}

	@Test
	public void testGetRoutingKeyTopic() {
		assertEquals("", Destinations.getRoutingKey(new Topic("topic")));
	}

	@Test
	public void testIsDestinationNotFoundQueue() {
		assertTrue(Destinations.isDestinationNotFound(new IOException("no queue")));
	}

	@Test
	public void testIsDestinationNotFoundTopic() {
		assertTrue(Destinations.isDestinationNotFound(new IOException("no exchange")));
	}

	@Test
	public void testIsDestinationNotFoundUnrelated() {
		assertFalse(Destinations.isDestinationNotFound(new IOException("connection reset")));
	}
}
