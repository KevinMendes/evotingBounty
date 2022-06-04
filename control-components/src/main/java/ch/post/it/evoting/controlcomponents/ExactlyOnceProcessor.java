/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandId;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;

/**
 * Class for processing requests exactly once.
 */
@Service
public class ExactlyOnceProcessor {

	private final CommandService commandService;
	@Value("${nodeID}")
	private int nodeId;

	public ExactlyOnceProcessor(final CommandService commandService) {
		this.commandService = commandService;
	}

	/**
	 * Processes a given task exactly once.
	 * <ul>
	 * <li>If the same request (same ids, same request content) is already stored in the database, the response to the request is taken from the database and returned.</li>
	 * <li>If the same request ids but different request content, an IllegalStateException is thrown.</li>
	 * <li>Otherwise the request is processed and the response is persisted and returned.</li>
	 * </ul>
	 *
	 * @param exactlyOnceTask The exactlyOnceTask to be processed.
	 * @return the result of the processing as a byte array.
	 * @throws NullPointerException  if the exactlyOnceTask is null.
	 * @throws IllegalStateException if the request could not be processed correctly.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public byte[] process(final ExactlyOnceTask exactlyOnceTask) {
		checkNotNull(exactlyOnceTask);
		final String correlationId = exactlyOnceTask.getCorrelationId();
		final String contextId = exactlyOnceTask.getContextId();
		final String context = exactlyOnceTask.getContext();
		final Callable<byte[]> task = exactlyOnceTask.getTask();
		final byte[] requestContent = exactlyOnceTask.getRequestContent();

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context)
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();
		final Optional<Command> identicalCommand = commandService.findSemanticallyIdenticalCommand(commandId);

		byte[] result;
		if (identicalCommand.isPresent()) {
			final byte[] requestPayload = identicalCommand.get().getRequestPayload();
			// Check if the identical command request payload is the same as the new request payload.
			if (Arrays.equals(requestContent, requestPayload)) {
				return identicalCommand.get().getResponsePayload();
			} else {
				final String errorMessage = String.format(
						"Similar request previously treated but for different request payload. [correlationId: %s, contextId: %s, context: %s, nodeId: %s]",
						correlationId, contextId, context, nodeId);
				throw new IllegalStateException(errorMessage);
			}
		} else {
			commandService.saveRequest(commandId, requestContent);
			try {
				result = task.call();
			} catch (Exception e) {
				throw new IllegalStateException("Failed to obtain response payload", e);
			}

			// Create and send response message.
			commandService.saveResponse(commandId, result);
			return result;
		}
	}
}
