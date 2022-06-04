/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.distributedprocessing.commands.CommandId;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;

@ActiveProfiles("test")
@SpringBootTest
@DisplayName("ExactlyOnceProcessor calling")
class ExactlyOnceProcessorTest {

	private static final String BAD_INPUT_ID = "Bad input";
	private static final RandomService randomService = new RandomService();
	private static final int RANDOM_SIZE = 32;

	@Value("${nodeID}")
	private int nodeId;

	@SpyBean
	private CommandService commandService;

	@SpyBean
	private ObjectMapper objectMapper;

	@SpyBean
	private ExactlyOnceProcessor processor;

	private String correlationId;
	private String contextId;
	private String context;

	@BeforeEach
	void setup() {
		correlationId = randomService.genRandomBase64String(RANDOM_SIZE);

		contextId = randomService.genRandomBase32String(RANDOM_SIZE);
		context = randomService.genRandomBase16String(RANDOM_SIZE);
	}

	@Test
	@DisplayName("processExactlyOnce with null argument does not save")
	void testProcessExactlyOnceWithNullArgumentsThrows() {
		assertThrows(NullPointerException.class, () -> processor.process(null));
	}

	@Test
	@DisplayName("processExactlyOnce with process throwing an exception does roll back correctly")
	void testProcessExactlyOnceWithProcessThrowingExceptionRollsBackCorrectly() throws JsonProcessingException {

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context)
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();

		assertFalse(commandService.findIdenticalCommand(commandId).isPresent());

		final TestPayload payload = new TestPayload(BAD_INPUT_ID);
		final byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);
		final Callable<byte[]> callable = () -> getTestPayloadBytes(payload);
		final ExactlyOnceTask processingInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(callable)
				.setRequestContent(payloadBytes)
				.build();

		final IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> processor.process(processingInput));
		assertEquals("Failed to obtain response payload", exception.getMessage());
		assertEquals(BAD_INPUT_ID, Throwables.getRootCause(exception).getMessage());

		assertFalse(commandService.findIdenticalCommand(commandId).isPresent());
	}

	@Test
	@DisplayName("processExactlyOnce with processing function returning a payload saves request and response")
	void testProcessExactlyOnceWithCallableReturningPayloadSavesResponseCorrectly() throws IOException {
		final TestPayload testPayload = new TestPayload("PayloadToBeSaved1");
		final byte[] testPayloadBytes = objectMapper.writeValueAsBytes(testPayload);
		final Callable<byte[]> callable = () -> getTestPayloadBytes(testPayload);
		final ExactlyOnceTask processingInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(callable)
				.setRequestContent(testPayloadBytes)
				.build();

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context)
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();

		assertFalse(commandService.findIdenticalCommand(commandId).isPresent());

		final byte[] payloadBytes = assertDoesNotThrow(() -> processor.process(processingInput));

		assertTrue(commandService.findIdenticalCommand(commandId).isPresent());
		assertEquals(objectMapper.writeValueAsString(testPayload), new String(payloadBytes));
	}

	@Test
	@DisplayName("processExactlyOnce twice with exactly the same message, saves request and response only once")
	void testProcessExactlyOnceWithMessageAlreadySavedDoesNotCallAgain() throws JsonProcessingException {
		final TestPayload testPayload = new TestPayload("PayloadToBeSaved2");
		final byte[] testPayloadBytes = objectMapper.writeValueAsBytes(testPayload);
		final Callable<byte[]> callable = () -> getTestPayloadBytes(testPayload);

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context)
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();

		assertFalse(commandService.findIdenticalCommand(commandId).isPresent());

		// 1. Call
		final ExactlyOnceTask task = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(callable)
				.setRequestContent(testPayloadBytes)
				.build();
		final byte[] responseBytes1 = assertDoesNotThrow(() -> processor.process(task));

		verify(commandService, times(1)).saveRequest(any(), any());
		verify(commandService, times(1)).saveResponse(any(), any());
		assertTrue(commandService.findIdenticalCommand(commandId).isPresent());
		assertEquals(objectMapper.writeValueAsString(testPayload), new String(responseBytes1));

		// 2.call
		final ExactlyOnceTask throwingTask = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(() -> {
					throw new NullPointerException("This should not be thrown.");
				})
				.setRequestContent(testPayloadBytes)
				.build();

		final byte[] responseBytes2 = assertDoesNotThrow(() -> processor.process(throwingTask));

		assertTrue(commandService.findIdenticalCommand(commandId).isPresent());
		assertEquals(objectMapper.writeValueAsString(testPayload), new String(responseBytes2));

	}

	@Test
	@DisplayName("processExactlyOnce with same message twice but with different message content throws an exception")
	void testProcessExactlyOnceWithMessageAlreadySavedButContentDifferentThrows() throws JsonProcessingException {
		final TestPayload payload = new TestPayload("PayloadToBeSaved3");
		final byte[] payloadBytes = objectMapper.writeValueAsBytes(payload);
		final Callable<byte[]> callable = () -> getTestPayloadBytes(payload);
		final ExactlyOnceTask processingInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(callable)
				.setRequestContent(payloadBytes)
				.build();

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context)
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();

		assertFalse(commandService.findIdenticalCommand(commandId).isPresent());

		// 1. Call
		assertDoesNotThrow(() -> processor.process(processingInput));
		assertTrue(commandService.findIdenticalCommand(commandId).isPresent());

		// 2.call
		final byte[] differentMessageBytes = new byte[] { 0b0000101 };
		final Callable<byte[]> differentCallable = () -> differentMessageBytes;
		final ExactlyOnceTask differentProcessingInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(context)
				.setTask(differentCallable)
				.setRequestContent(differentMessageBytes)
				.build();

		final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> processor.process(differentProcessingInput));
		final String expectedErrorMessage = String.format(
				"Similar request previously treated but for different request payload. [correlationId: %s, contextId: %s, context: %s, nodeId: %s]",
				correlationId, contextId, context, nodeId);
		assertEquals(expectedErrorMessage, Throwables.getRootCause(exception).getMessage());
		assertTrue(commandService.findIdenticalCommand(commandId).isPresent());

	}

	private byte[] getTestPayloadBytes(final TestPayload testPayload) throws JsonProcessingException {
		if (testPayload.getId().equals(BAD_INPUT_ID)) {
			throw new IllegalStateException(BAD_INPUT_ID);
		}
		return objectMapper.writeValueAsBytes(testPayload);
	}

	static class TestPayload {

		@JsonProperty
		private final String id;

		@JsonCreator
		public TestPayload(
				@JsonProperty("id")
				final String id) {
			this.id = id;
		}

		public String getId() {
			return this.id;
		}
	}
}