/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;

import ch.post.it.evoting.domain.election.model.messaging.SafeStreamDeserializationException;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializable;
import ch.post.it.evoting.domain.election.model.messaging.StreamSerializableClassType;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriter;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Topic;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;

/**
 * Tests of {@link AbstractPartialResultsListener}.
 */
public class AbstractPartialResultsListenerTest {
	private static final UUID CORRELATION_ID = new UUID(1, 1);

	private static final PartialResult RESULT1 = new PartialResult();

	private static final PartialResult RESULT2 = new PartialResult();

	private static final byte[] BYTES1 = { 1 };

	private static final byte[] BYTES2 = { 2 };

	private static final int PARTIAL_RESULT_COUNT = 2;

	private static final Topic TOPIC = new Topic("or-ha");

	private MessagingService messagingService;

	private PartialResultsRepository<byte[]> repository;

	private StreamSerializableObjectWriter writer;

	private TestablePartialResultsListener listener;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws IOException {
		messagingService = mock(MessagingService.class);

		repository = mock(PartialResultsRepository.class);
		when(repository.hasAll(CORRELATION_ID, PARTIAL_RESULT_COUNT)).thenReturn(false, true);

		writer = mock(StreamSerializableObjectWriter.class);
		when(writer.write(RESULT1)).thenReturn(BYTES1);
		when(writer.write(RESULT2)).thenReturn(BYTES2);

		listener = new TestablePartialResultsListener(writer, repository, messagingService, TOPIC);
	}

	@Test
	public void testOnMessageResultsPending() throws MessagingException {
		listener.onMessage(new Message(CORRELATION_ID, RESULT1));
		verify(repository).save(CORRELATION_ID, BYTES1);
		verify(messagingService, never()).send(eq(TOPIC), any());
	}

	@Test
	public void testOnMessageResultsReady() throws MessagingException {
		listener.onMessage(new Message(CORRELATION_ID, RESULT1));
		listener.onMessage(new Message(CORRELATION_ID, RESULT2));
		verify(repository).save(CORRELATION_ID, BYTES1);
		verify(repository).save(CORRELATION_ID, BYTES2);
		byte[] notification = { 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1 };
		verify(messagingService).send(TOPIC, notification);
	}

	private static class Message {
		private final UUID correlationId;

		private final PartialResult result;

		public Message(UUID correlationId, PartialResult result) {
			this.correlationId = correlationId;
			this.result = result;
		}
	}

	private static class PartialResult implements StreamSerializable {

		@Override
		public void deserialize(MessageUnpacker unpacker) throws SafeStreamDeserializationException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void serialize(MessagePacker packer) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public StreamSerializableClassType type() {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestablePartialResultsListener extends AbstractPartialResultsListener {

		public TestablePartialResultsListener(StreamSerializableObjectWriter writer, PartialResultsRepository<byte[]> repository,
				MessagingService messagingService, Topic topic) {
			super(writer, repository, messagingService, topic);
		}

		@Override
		protected UUID getCorrelationId(Object message) {
			return ((Message) message).correlationId;
		}

		@Override
		protected StreamSerializable getPartialResult(Object message) {
			return ((Message) message).result;
		}

		@Override
		protected int partialResultCount() {
			return PARTIAL_RESULT_COUNT;
		}
	}
}
