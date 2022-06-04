/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

/**
 * Provides message related functionalities
 */
public class Messages {

	private Messages() {
		// hide the implicit public constructor
	}

	/**
	 * Creates a message with the given correlation id and content.
	 *
	 * @param correlationId  The correlation id of the message. Not null.
	 * @param messageContent The content of the message. Not null.
	 * @return a new message with the given correlation id and content.
	 * @throws NullPointerException if the correlation id or the message content is null.
	 */
	public static Message createMessage(final String correlationId, final byte[] messageContent) {
		checkNotNull(correlationId);
		checkNotNull(messageContent);
		final MessageProperties properties = new MessageProperties();
		properties.setCorrelationId(correlationId);
		return new Message(messageContent, properties);
	}
}
