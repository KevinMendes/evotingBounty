/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes.safestream;

import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

public interface StreamSerializableObjectReader<T extends StreamSerializable> {

	T read(byte[] serializedObject) throws SafeStreamDeserializationException;

	T read(byte[] serializedObject, int offset, int length) throws SafeStreamDeserializationException;
}
