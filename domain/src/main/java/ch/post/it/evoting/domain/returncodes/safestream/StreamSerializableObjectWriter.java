/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.returncodes.safestream;

import java.io.IOException;

import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;

public interface StreamSerializableObjectWriter {

	byte[] write(StreamSerializable streamSerializableObject) throws IOException;

	byte[] write(StreamSerializable streamSerializableObject, int offset) throws IOException;
}
