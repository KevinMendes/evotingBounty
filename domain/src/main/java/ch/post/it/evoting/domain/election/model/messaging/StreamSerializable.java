/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.messaging;

import java.io.IOException;

import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

/**
 * Interface to provide serialization and deserialization functionalities using MsgPack.
 */
public interface StreamSerializable {

	/**
	 * The class implementing this interface must implement the serialization of the class to MsgPack format. The implementation must be compatible
	 * (meaning the same fields in the same order) with the {@link #deserialize(MessageUnpacker)} method.
	 *
	 * @param packer The packer that is used for storing the data of the class.
	 * @throws IOException In case the serialization cannot happen.
	 */
	void serialize(MessagePacker packer) throws IOException;

	/**
	 * The class implementing this interface must implement the deserialization of the class from MsgPack format.The implementation must be compatible
	 * (meaning the same fields in the same order) with the {@link #serialize(MessagePacker)} method.
	 *
	 * @param unpacker
	 * @throws SafeStreamDeserializationException
	 */
	void deserialize(MessageUnpacker unpacker) throws SafeStreamDeserializationException;

	/**
	 * Returns the type of the class.
	 *
	 * @return The Type of the class that implements the interface.
	 */
	StreamSerializableClassType type();
}
