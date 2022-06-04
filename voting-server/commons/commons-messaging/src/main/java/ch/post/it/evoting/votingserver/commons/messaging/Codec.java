/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import javax.annotation.concurrent.ThreadSafe;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

/**
 * Codec to encode and to decode messages.
 */
@ThreadSafe
interface Codec {
	/**
	 * Decodes the message from given bytes
	 *
	 * @param bytes the bytes
	 * @return the message
	 * @throws InvalidMessageException failed to decode the message.
	 */
	Object decode(byte[] bytes) throws InvalidMessageException;

	/**
	 * Encodes a given message. The message must be one of the following:
	 * <ul>
	 * <li>{@code byte[]}</li>
	 * <li>{@link StreamSerializable}</li>
	 * </ul>
	 * otherwise {@link InvalidMessageException} is thrown.
	 *
	 * @param message the message
	 * @return the encoded message
	 * @throws InvalidMessageException the message is invalid
	 */
	byte[] encode(Object message) throws InvalidMessageException;
}
