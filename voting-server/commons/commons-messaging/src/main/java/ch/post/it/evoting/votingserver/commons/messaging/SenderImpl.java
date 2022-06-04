/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.WillCloseWhenClosed;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

/**
 * Implementation of {@link Sender}.
 */
class SenderImpl implements Sender {
	private final Channel channel;

	private final Codec codec;

	private boolean valid = true;

	/**
	 * Constructor.
	 *
	 * @param channel
	 * @param codec
	 */
	public SenderImpl(
			@WillCloseWhenClosed
					Channel channel, Codec codec) {
		this.channel = channel;
		this.codec = codec;
	}

	@Override
	public void destroy() throws MessagingException {
		try {
			channel.close();
		} catch (IOException | TimeoutException e) {
			throw new MessagingException("Failed to destroy sender.", e);
		}
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public void send(Destination destination, Object message) throws MessagingException {
		sendMessage(destination, message);
	}

	/**
	 * Sends a message to a destination.
	 *
	 * @param destination where the message should be sent
	 * @param message     the message to send
	 * @throws MessagingException if a message could not be sent
	 */
	private void sendMessage(Destination destination, Object message) throws MessagingException {
		String exchange = Destinations.getExchange(destination);
		String routingKey = Destinations.getRoutingKey(destination);
		byte[] body = codec.encode(message);
		try {
			channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_BASIC, body);
		} catch (IOException e) {
			if (Destinations.isDestinationNotFound(e)) {
				throw new DestinationNotFoundException(format("Destination ''{0}'' does not exist.", destination), e);
			}
			valid = false;
			throw new MessagingException("Failed to send the message.", e);
		}
	}
}
