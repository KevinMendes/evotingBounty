/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import javax.annotation.WillCloseWhenClosed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * Implementation of {@link Receiver}.
 */
public class ReceiverImpl extends DefaultConsumer implements Receiver {
	private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverImpl.class);

	private final Destination destination;

	private final MessageListener listener;

	private final Executor executor;

	private final Codec codec;

	private boolean destroyed;

	/**
	 * Constructor.
	 *
	 * @param channel
	 * @param destination
	 * @param listener
	 * @param executor
	 * @param codec
	 */
	public ReceiverImpl(
			@WillCloseWhenClosed
					Channel channel, Destination destination, MessageListener listener, Executor executor, Codec codec) {
		super(channel);

		LOGGER.debug("Starting receiver on {}...", destination.name());
		this.destination = destination;
		this.listener = listener;
		this.executor = executor;
		this.codec = codec;
		LOGGER.info("Receiver for {} started", destination.name());
	}

	@Override
	public void destroy() throws MessagingException {
		if (!destroyed) {
			try {
				if (getConsumerTag() != null) {
					getChannel().basicCancel(getConsumerTag());
					LOGGER.info("No longer receiving from {}", destination.name());
				}
				getChannel().close();
			} catch (IOException | TimeoutException e) {
				throw new MessagingException("Failed to destroy receiver.", e);
			}
			destroyed = true;
		}
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) {
		try {
			deliverMessage(decodeMessage(body));
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while processing a message.", e);
		}

	}

	@Override
	public void receive() throws MessagingException {
		if (destroyed) {
			throw new MessagingException("Receiver is already destroyed.");
		}
		String queue = getQueue();
		try {
			getChannel().basicConsume(queue, true, this);
		} catch (IOException e) {
			if (Destinations.isDestinationNotFound(e)) {
				throw new DestinationNotFoundException(format("Queue ''{0}'' does not exist.", queue), e);
			}
			throw new MessagingException("Failed to receive messages.", e);
		}
	}

	private Object decodeMessage(byte[] body) throws IOException {
		try {
			return codec.decode(body);
		} catch (InvalidMessageException e) {
			throw new IOException("Failed to decode message.", e);
		}
	}

	private void deliverMessage(Object message) {
		executor.execute(() -> listener.onMessage(message));
	}

	private String getQueue() throws MessagingException {
		if (destination instanceof Queue) {
			return destination.name();
		} else if (destination instanceof Topic) {
			String queue;
			try {
				queue = getChannel().queueDeclare().getQueue();
			} catch (IOException e) {
				throw new MessagingException("Failed to get queue.", e);
			}
			String exchange = Destinations.getExchange(destination);
			String routingKey = Destinations.getRoutingKey(destination);
			try {
				getChannel().queueBind(queue, exchange, routingKey);
			} catch (IOException e) {
				if (Destinations.isDestinationNotFound(e)) {
					throw new DestinationNotFoundException(format("Topic ''{0}'' does not exist.", exchange), e);
				}
				throw new MessagingException("Failed to get queue.", e);
			}
			return queue;
		} else {
			throw new IllegalArgumentException(format("Unsupported destination ''{0}''.", destination));
		}
	}
}
