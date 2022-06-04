/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandId;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.MessageBrokerOrchestratorApplication;

/**
 * Provides reusability and transactionality for command service.
 */
@Service
public class CommandFacade {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandFacade.class);

	private final CommandService commandService;

	public CommandFacade(final CommandService commandService) {
		this.commandService = commandService;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveRequest(final byte[] payload, final String correlationId, final String contextId, final Context context,
			final int nodeId) {

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context.toString())
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();
		commandService.saveRequest(commandId, payload);
		LOGGER.info("Saved request [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void saveResponse(final byte[] encodedResponse, final String correlationId, final String contextId, final Context context,
			final int nodeId) {

		final CommandId commandId = new CommandId.Builder()
				.contextId(contextId)
				.context(context.toString())
				.correlationId(correlationId)
				.nodeId(nodeId)
				.build();
		commandService.saveResponse(commandId, encodedResponse);

		LOGGER.info("Saved response [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);
	}

	/**
	 * Get all node responses.
	 * @param correlationId the id for which to search for the responses.
	 * @return the list of commands-
	 * @throws IllegalStateException if the number of commands found is not the expected.
	 */
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public List<Command> getAllNodesResponses(String correlationId) {
		final List<Command> allMessagesWithCorrelationId = commandService.findAllCommandsWithResponsePayload(correlationId);
		if (allMessagesWithCorrelationId.size() != MessageBrokerOrchestratorApplication.NODE_IDS.size()) {
			throw new IllegalStateException(String.format("The number of node response is invalid. [expected: %s, actual: %s]",
					MessageBrokerOrchestratorApplication.NODE_IDS.size(), allMessagesWithCorrelationId.size()));
		}
		return allMessagesWithCorrelationId;
	}
}
