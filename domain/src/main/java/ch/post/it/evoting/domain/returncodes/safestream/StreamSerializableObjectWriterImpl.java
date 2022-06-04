/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes.safestream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

public class StreamSerializableObjectWriterImpl implements StreamSerializableObjectWriter {

	@Override
	public byte[] write(final StreamSerializable streamSerializableObject, final int offset) throws IOException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < offset; i++) {
			out.write(0);
		}
		try (final MessagePacker packer = MessagePack.newDefaultPacker(out)) {
			packer.packString(streamSerializableObject.type().name());
			streamSerializableObject.serialize(packer);
		}
		return out.toByteArray();
	}

	@Override
	public byte[] write(final StreamSerializable streamSerializableObject) throws IOException {
		return write(streamSerializableObject, 0);
	}
}
