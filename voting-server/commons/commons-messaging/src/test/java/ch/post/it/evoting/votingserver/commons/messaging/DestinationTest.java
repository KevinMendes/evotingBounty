/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Tests of {@link Destination}.
 */
public class DestinationTest {
	@Test
	public void testEqualsObject() {
		Destination destination1 = new Queue("queue");
		Destination destination2 = new Queue("queue");
		Destination destination3 = new Queue("queue3");

		Destination destination4 = new Topic("topic");
		Destination destination5 = new Topic("topic");
		Destination destination6 = new Topic("topic6");

		Destination destination7 = new Topic("queue");

		assertEquals(destination1, destination1);
		assertEquals(destination1, destination2);
		assertNotEquals(destination1, destination3);

		assertEquals(destination4, destination4);
		assertEquals(destination4, destination5);
		assertNotEquals(destination4, destination6);

		assertNotEquals(destination1, destination7);
	}
}
