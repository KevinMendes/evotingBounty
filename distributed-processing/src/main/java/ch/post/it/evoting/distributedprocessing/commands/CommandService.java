/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.distributedprocessing.commands;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommandService {

	private final CommandRepository commandRepository;

	public CommandService(final CommandRepository commandRepository) {
		this.commandRepository = commandRepository;
	}

	public void saveRequest(final CommandId commandId, final byte[] payload) {
		checkNotNull(commandId);
		checkNotNull(payload);

		final Command command = new Command.Builder()
				.commandId(commandId)
				.requestPayload(payload)
				.requestDateTime(LocalDateTime.now())
				.build();
		commandRepository.save(command);
	}

	public boolean existsByIdCommandId(CommandId commandId) {
		return commandRepository.existsById(commandId);
	}

	public boolean isRequestForContextIdAndContextAlreadyPresent(final String contextId, final String context) {
		checkNotNull(contextId);
		checkNotNull(context);

		return commandRepository.existsByContextIdAndContext(contextId, context);
	}

	public Command saveResponse(final CommandId commandId, final byte[] responsePayload) {
		checkNotNull(commandId);
		checkNotNull(responsePayload);

		final Command command = findCommand(commandId);
		command.setResponsePayload(responsePayload);
		command.setResponseDateTime(LocalDateTime.now());
		return commandRepository.save(command);
	}

	private Command findCommand(final CommandId commandId) {
		checkNotNull(commandId);

		final Optional<Command> message = commandRepository.findById(commandId);
		return message.orElseThrow(() -> new IllegalStateException(String.format("Could not find a matching command. [CommandId: %s]", commandId)));
	}

	public List<Command> findAllCommandsWithCorrelationId(final String correlationId) {
		checkNotNull(correlationId);

		return commandRepository.findAllByCorrelationId(correlationId);
	}

	public List<Command> findAllCommandsWithResponsePayload(final String correlationId) {
		checkNotNull(correlationId);

		return commandRepository.findAllByCorrelationIdAndResponsePayloadIsNotNull(correlationId);
	}

	/**
	 * Count the number of commands with a response payload for this correlationId.
	 *
	 * @param correlationId the correlationId to aggregate on
	 * @return the count
	 */
	public Integer countAllCommandsWithResponsePayload(final String correlationId) {
		return commandRepository.countByCorrelationIdAndResponsePayloadIsNotNull(correlationId);
	}

	public Optional<Command> findIdenticalCommand(final CommandId commandId) {
		checkNotNull(commandId);

		return commandRepository.findById(commandId);
	}

	public Optional<Command> findSemanticallyIdenticalCommand(final CommandId commandId) {
		checkNotNull(commandId);

		final String contextId = commandId.getContextId();
		final String context = commandId.getContext();
		final Integer nodeId = commandId.getNodeId();

		return commandRepository.findByContextIdAndContextAndNodeId(contextId, context, nodeId);
	}
}
