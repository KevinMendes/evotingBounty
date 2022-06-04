/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.MessageProperties;

/**
 * Tests of {@link ReceiverImpl}.
 */
public class ReceiverImplTest {
	private static final Queue QUEUE = new Queue("queue");

	private static final Topic TOPIC = new Topic("topic");

	private static final byte[] MESSAGE = { 1, 2, 3 };

	private static final String CONSUMER_TAG = "consumerTag";

	private Channel channel;

	private MessageListener listener;

	private Executor executor;

	private Codec codec;

	private ReceiverImpl receiver;

	@Before
	public void setUp() throws IOException {
		channel = mock(Channel.class);

		listener = mock(MessageListener.class);
		executor = CurrentThreadExecutor.getInstance();
		codec = CodecImpl.getInstance();
		receiver = new ReceiverImpl(channel, QUEUE, listener, executor, codec);

		when(channel.basicConsume(QUEUE.name(), true, receiver)).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				receiver.handleConsumeOk(CONSUMER_TAG);
				return CONSUMER_TAG;
			}
		});
	}

	@Test
	public void testDestroyAfterReceive() throws MessagingException, IOException, TimeoutException {
		receiver.receive();
		receiver.destroy();
		verify(channel).basicCancel(CONSUMER_TAG);
		verify(channel).close();
	}

	@Test
	public void testDestroyBeforeReceive() throws MessagingException, IOException, TimeoutException {
		receiver.destroy();
		verify(channel, never()).basicCancel(CONSUMER_TAG);
		verify(channel).close();
	}

	@Test(expected = MessagingException.class)
	public void testDestroyError() throws IOException, MessagingException {
		doThrow(new IOException("test")).when(channel).basicCancel(CONSUMER_TAG);
		receiver.receive();
		receiver.destroy();
	}

	@Test
	public void testHandleDeliveryStringEnvelopeBasicPropertiesByteArray() throws InvalidMessageException, IOException {
		Envelope envelope = new Envelope(0, false, "", QUEUE.name());
		BasicProperties properties = MessageProperties.PERSISTENT_BASIC;
		byte[] body = codec.encode(MESSAGE);
		receiver.handleDelivery(CONSUMER_TAG, envelope, properties, body);
		verify(listener).onMessage(eq(MESSAGE));
	}

	public void testHandleDeliveryStringEnvelopeBasicPropertiesByteArrayInvalidBody() throws InvalidMessageException, IOException {
		Envelope envelope = new Envelope(0, false, "", QUEUE.name());
		BasicProperties properties = MessageProperties.PERSISTENT_BASIC;
		byte[] body = { 2, 3, 4 };
		try {
			receiver.handleDelivery(CONSUMER_TAG, envelope, properties, body);
		} finally {
			verify(listener, never()).onMessage(any());
		}
	}

	@Test
	public void testReceive() throws MessagingException, IOException {
		receiver.receive();
		verify(channel).basicConsume(QUEUE.name(), true, receiver);
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testReceiveDestinationNotFound() throws MessagingException, IOException {
		when(channel.basicConsume(QUEUE.name(), true, receiver)).thenThrow(new IOException("no queue"));
		receiver.receive();
	}

	@Test(expected = MessagingException.class)
	public void testReceiveDestroyed() throws MessagingException {
		receiver.destroy();
		receiver.receive();
	}

	@Test(expected = MessagingException.class)
	public void testReceiveError() throws IOException, MessagingException {
		when(channel.basicConsume(QUEUE.name(), true, receiver)).thenThrow(new IOException("test"));
		receiver.receive();
	}

	@Test
	public void testReceiveTopic() throws IOException, MessagingException {
		receiver = new ReceiverImpl(channel, TOPIC, listener, executor, codec);

		DeclareOk declareOk = mock(DeclareOk.class);
		when(declareOk.getQueue()).thenReturn(QUEUE.name());
		when(channel.queueDeclare()).thenReturn(declareOk);

		when(channel.basicConsume(QUEUE.name(), true, receiver)).then(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				receiver.handleConsumeOk(CONSUMER_TAG);
				return CONSUMER_TAG;
			}
		});

		receiver.receive();

		verify(channel).queueBind(QUEUE.name(), TOPIC.name(), "");
		verify(channel).basicConsume(QUEUE.name(), true, receiver);
	}

	@Test(expected = DestinationNotFoundException.class)
	public void testReceiveTopicDestinationNotFound() throws IOException, MessagingException {
		receiver = new ReceiverImpl(channel, TOPIC, listener, executor, codec);

		DeclareOk declareOk = mock(DeclareOk.class);
		when(declareOk.getQueue()).thenReturn(QUEUE.name());
		when(channel.queueDeclare()).thenReturn(declareOk);

		when(channel.queueBind(QUEUE.name(), TOPIC.name(), "")).thenThrow(new IOException("no exchange"));

		receiver.receive();
	}
}
