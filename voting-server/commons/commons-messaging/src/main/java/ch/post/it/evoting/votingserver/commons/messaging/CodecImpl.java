/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.nio.ByteBuffer;

import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectReader;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectReaderImpl;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriter;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriterImpl;

class CodecImpl implements Codec {
	private static final CodecImpl INSTANCE = new CodecImpl();

	private static final byte CONTENT_TYPE_BINARY = 0;

	private static final byte CONTENT_TYPE_STREAM_SERIALIZABLE = 1;

	private CodecImpl() {
	}

	public static CodecImpl getInstance() {
		return INSTANCE;
	}

	private static byte[] decodeBinary(ByteBuffer buffer) {
		byte[] message = new byte[buffer.remaining()];
		buffer.get(message);
		return message;
	}

	private static StreamSerializable decodeStreamSerialazable(ByteBuffer buffer) throws InvalidMessageException {
		StreamSerializableObjectReader<StreamSerializable> reader = new StreamSerializableObjectReaderImpl<>();
		try {
			return reader.read(buffer.array(), buffer.position(), buffer.remaining());
		} catch (SafeStreamDeserializationException e) {
			throw new InvalidMessageException("Failed to decode message.", e);
		}
	}

	private static byte[] encodeBinary(byte[] message) {
		byte[] bytes = new byte[message.length + 1];
		System.arraycopy(message, 0, bytes, 1, message.length);
		bytes[0] = CONTENT_TYPE_BINARY;
		return bytes;
	}

	private static byte[] encodeStreamSerializable(StreamSerializable message) throws InvalidMessageException {
		StreamSerializableObjectWriter writer = new StreamSerializableObjectWriterImpl();
		byte[] bytes;
		try {
			bytes = writer.write(message, 1);
		} catch (IOException e) {
			throw new InvalidMessageException("Failed to encode message.", e);
		}
		bytes[0] = CONTENT_TYPE_STREAM_SERIALIZABLE;
		return bytes;
	}

	@Override
	public Object decode(byte[] bytes) throws InvalidMessageException {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		if (!buffer.hasRemaining()) {
			throw new InvalidMessageException("Content type is missing.");
		}
		byte contentType = buffer.get();
		switch (contentType) {
		case CONTENT_TYPE_BINARY:
			return decodeBinary(buffer);
		case CONTENT_TYPE_STREAM_SERIALIZABLE:
			return decodeStreamSerialazable(buffer);
		default:
			throw new InvalidMessageException(format("Unsupported content type ''{0}''", contentType));
		}
	}

	@Override
	public byte[] encode(Object message) throws InvalidMessageException {
		if (message instanceof byte[]) {
			return encodeBinary((byte[]) message);
		} else if (message instanceof StreamSerializable) {
			return encodeStreamSerializable((StreamSerializable) message);
		} else {
			throw new InvalidMessageException(format("Invalid message class ''{0}''", message.getClass()));
		}
	}
}
