/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

/**
 * Tests of the sender.
 */
public class SenderImplTest {
	private static final Queue QUEUE = new Queue("queue");

	private static final Topic TOPIC = new Topic("topic");

	private static final byte[] MESSAGE = { 1, 2, 3 };

	private static final byte[] BODY = { 4, 5, 6 };

	private Channel channel;

	private Codec codec;

	private SenderImpl sender;

	@Before
	public void setUp() throws InvalidMessageException {
		channel = mock(Channel.class);

		codec = mock(Codec.class);
		when(codec.encode(MESSAGE)).thenReturn(BODY);

		sender = new SenderImpl(channel, codec);
	}

	@Test
	public void testDestroy() throws MessagingException, IOException, TimeoutException {
		sender.destroy();
		verify(channel).close();
	}

	@Test(expected = MessagingException.class)
	public void testDestroyError() throws MessagingException, IOException, TimeoutException {
		doThrow(new IOException("test")).when(channel).close();
		sender.destroy();
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testSendDestinationNotFoundQueue() throws IOException, MessagingException {
		doThrow(new IOException("no queue")).when(channel).basicPublish("", QUEUE.name(), MessageProperties.PERSISTENT_BASIC, BODY);
		sender.send(QUEUE, MESSAGE);
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testSendDestinationNotFoundTopic() throws IOException, MessagingException {
		doThrow(new IOException("no exchange")).when(channel).basicPublish(TOPIC.name(), "", MessageProperties.PERSISTENT_BASIC, BODY);
		sender.send(TOPIC, MESSAGE);
	}

	@Test(expected = MessagingException.class)
	public void testSendError() throws IOException, MessagingException {
		doThrow(new IOException("connection reset")).when(channel).basicPublish("", QUEUE.name(), MessageProperties.PERSISTENT_BASIC, BODY);
		try {
			sender.send(QUEUE, MESSAGE);
		} finally {
			assertFalse(sender.isValid());
		}
	}

	@Test(expected = InvalidMessageException.class)
	public void testSendInvalidMessage() throws MessagingException {
		when(codec.encode(MESSAGE)).thenThrow(new InvalidMessageException("test"));
		sender.send(QUEUE, MESSAGE);
	}

	@Test
	public void testSendQueue() throws MessagingException, IOException {
		sender.send(QUEUE, MESSAGE);
		verify(channel).basicPublish("", QUEUE.name(), MessageProperties.PERSISTENT_BASIC, BODY);
	}

	@Test
	public void testSendTopic() throws IOException, MessagingException {
		sender.send(TOPIC, MESSAGE);
		verify(channel).basicPublish(TOPIC.name(), "", MessageProperties.PERSISTENT_BASIC, BODY);
	}
}
