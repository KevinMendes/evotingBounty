/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes.safestream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

public class StreamSerializableObjectReaderImpl<T extends StreamSerializable> implements StreamSerializableObjectReader<T> {

	@SuppressWarnings("unchecked")
	@Override
	public T read(final byte[] serializedObject, final int offset, final int length) throws SafeStreamDeserializationException {
		try (final MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(new ByteArrayInputStream(serializedObject, offset, length))) {
			final String className = unpacker.unpackString();

			final T deserializedObject = (T) StreamSerializableUtil.resolveByName(className);
			deserializedObject.deserialize(unpacker);
			return deserializedObject;
		} catch (final IOException e) {
			throw new SafeStreamDeserializationException(e);
		}
	}

	@Override
	public T read(final byte[] serializedObject) throws SafeStreamDeserializationException {
		return read(serializedObject, 0, serializedObject.length);
	}

}
