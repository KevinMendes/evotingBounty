/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.distributedprocessing.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CommandServiceTest {

	private static final String CONTEXT_KEY_GENERATION = "key-generation";
	private static final String CONTEXT_ID_ONE = "context-id-1";
	private static final String CORRELATION_ID_ONE = "1";
	private static final String CORRELATION_ID_THREE = "3";
	private static final int NODE_ID_ONE = 1;
	private static final int NODE_ID_2 = 2;
	private static SecureRandom secureRandom;
	private Logger LOGGER = LoggerFactory.getLogger(CommandServiceTest.class);

	@Autowired
	CommandService commandService;

	@Autowired
	CommandRepository commandRepository;

	@BeforeAll
	public static void bootstrap(
			@Autowired
			final
			CommandService commandService) {

		final CommandId commandIdOneNodeOne = new CommandId.Builder()
				.contextId(CONTEXT_ID_ONE)
				.context(CONTEXT_KEY_GENERATION)
				.correlationId(CORRELATION_ID_ONE)
				.nodeId(NODE_ID_ONE)
				.build();
		final CommandId commandIdOneNodeTwo = new CommandId.Builder()
				.contextId(CONTEXT_ID_ONE)
				.context(CONTEXT_KEY_GENERATION)
				.correlationId(CORRELATION_ID_ONE)
				.nodeId(NODE_ID_2)
				.build();

		secureRandom = new SecureRandom();
		final byte[] requestPayload = new byte[10];

		secureRandom.nextBytes(requestPayload);
		commandService.saveRequest(commandIdOneNodeOne, requestPayload);
		commandService.saveRequest(commandIdOneNodeTwo, requestPayload);
	}

	@Test
	void findIdenticalCommand() {
		final CommandId commandIdOneNodeOne = new CommandId.Builder().contextId(CONTEXT_ID_ONE).context(CONTEXT_KEY_GENERATION).correlationId(
						CORRELATION_ID_ONE).nodeId(NODE_ID_ONE)
				.build();

		final Optional<Command> identicalCommand = commandService.findIdenticalCommand(commandIdOneNodeOne);
		assertTrue(identicalCommand.isPresent());
	}

	@Test
	void findSemanticallyIdenticalCommand() {
		final CommandId commandIdOneNodeTwo = new CommandId.Builder().contextId(CONTEXT_ID_ONE).context(CONTEXT_KEY_GENERATION).correlationId("2").nodeId(
						NODE_ID_ONE)
				.build();

		final Optional<Command> identicalCommand = commandService.findIdenticalCommand(commandIdOneNodeTwo);
		assertFalse(identicalCommand.isPresent());

		final Optional<Command> semanticallyIdenticalCommand = commandService.findSemanticallyIdenticalCommand(commandIdOneNodeTwo);
		assertTrue(semanticallyIdenticalCommand.isPresent());
	}

	@Test
	void failToFindCommand() {
		final CommandId commandIdTwoNodeTwo = new CommandId.Builder().contextId("unique-id-2").context(CONTEXT_KEY_GENERATION).correlationId(
						CORRELATION_ID_ONE).nodeId(NODE_ID_ONE)
				.build();

		final Optional<Command> identicalCommand = commandService.findIdenticalCommand(commandIdTwoNodeTwo);
		assertFalse(identicalCommand.isPresent());

		final Optional<Command> semanticallyIdenticalCommand = commandService.findSemanticallyIdenticalCommand(commandIdTwoNodeTwo);
		assertFalse(semanticallyIdenticalCommand.isPresent());
	}

	@Test
	void saveResponseSuccessfullyFindRequest() throws NoSuchAlgorithmException {
		final CommandId commandIdOneNodeOne = new CommandId.Builder()
				.contextId(CONTEXT_ID_ONE)
				.context(CONTEXT_KEY_GENERATION)
				.correlationId(CORRELATION_ID_ONE)
				.nodeId(NODE_ID_ONE)
				.build();

		final boolean requestForContextIdAndContextAlreadyPresent = commandService.isRequestForContextIdAndContextAlreadyPresent(CONTEXT_ID_ONE,
				CONTEXT_KEY_GENERATION);
		assertTrue(requestForContextIdAndContextAlreadyPresent);

		final byte[] responsePayload = new byte[10];
		secureRandom.nextBytes(responsePayload);

		final Command command = commandService.saveResponse(commandIdOneNodeOne, responsePayload);
		assertNotNull(command.getResponsePayload());

	}

	@Test
	void saveResponseFailingToFindRequest() throws NoSuchAlgorithmException {
		final CommandId commandIdOneNodeOne = new CommandId.Builder()
				.contextId(CONTEXT_ID_ONE)
				.context(CONTEXT_KEY_GENERATION)
				.correlationId(CORRELATION_ID_THREE)
				.nodeId(NODE_ID_ONE)
				.build();

		final byte[] responsePayload = new byte[10];
		secureRandom.nextBytes(responsePayload);
		assertThrows(IllegalStateException.class, () -> commandService.saveResponse(commandIdOneNodeOne, responsePayload));

	}

	@Test
	void findAllMessagesWithCorrelationId() {
		final List<Command> allMessagesWithCorrelationId = commandService.findAllCommandsWithCorrelationId(CORRELATION_ID_ONE);
		assertEquals(NODE_ID_2, allMessagesWithCorrelationId.size());
	}

	@Disabled
	void testLocking() throws InterruptedException {

		//Command all ready exists in database, populated by @BeforeAll
		final CommandId commandID = new CommandId.Builder()
				.correlationId(CORRELATION_ID_ONE)
				.contextId(CONTEXT_ID_ONE)
				.context(CONTEXT_KEY_GENERATION)
				.nodeId(NODE_ID_ONE)
				.build();

		final CountDownLatch countDownLatch = new CountDownLatch(NODE_ID_2);

		final ExecutorService executorService = Executors.newFixedThreadPool(NODE_ID_2, new CustomizableThreadFactory("lock-"));

		executorService.execute(() ->
		{
			//TestTransaction.start(); I would have thought this should work as to my knowledge the threads are the transaction boundary (and this is a new thread).
			commandService.findIdenticalCommand(commandID);
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
			}
			LOGGER.info("ONE");
			countDownLatch.countDown();
			//TestTransaction.end();

		});

		executorService.execute(() -> {
			//TestTransaction.start();
			final byte[] responsePayload = new byte[10];
			secureRandom.nextBytes(responsePayload);

			final Optional<Command> command = commandService.findIdenticalCommand(commandID);
			commandService.saveResponse(command.get().getCommandId(), responsePayload);
			LOGGER.info("TWO");
			countDownLatch.countDown();
			//TestTransaction.end();
		});
		countDownLatch.await();
	}

}