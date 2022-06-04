/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

/**
 * Saves and retrieves the election event context - in interaction with the control components.
 */
@Stateless
public class ElectionEventContextService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventContextService.class);

	private final ObjectMapper objectMapper;
	private final ElectionEventContextRepository electionEventContextRepository;
	private final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient;

	@Inject
	ElectionEventContextService(final ObjectMapper objectMapper, final ElectionEventContextRepository electionEventContextRepository,
			final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient) {
		this.objectMapper = objectMapper;
		this.electionEventContextRepository = electionEventContextRepository;
		this.messageBrokerOrchestratorClient = messageBrokerOrchestratorClient;
	}

	/**
	 * Saves the election event context.
	 *
	 * @param electionEventContextPayload the request payload.
	 * @return the ElectionContextResponsePayloads of all the control components.
	 * @throws DuplicateEntryException if the election event context is already saved in the database.
	 * @throws UncheckedIOException    if an error occurs while serializing the election event context.
	 * @throws RetrofitException       if an error occurs while saving from the message broker orchestrator.
	 */
	public List<ElectionContextResponsePayload> saveElectionEventContext(final ElectionEventContextPayload electionEventContextPayload)
			throws DuplicateEntryException, UncheckedIOException, RetrofitException {
		checkNotNull(electionEventContextPayload);

		final String electionEventId = electionEventContextPayload.getElectionEventContext().getElectionEventId();

		// save election event context in vote verification
		electionEventContextRepository.save(getElectionEventContextEntity(electionEventContextPayload.getElectionEventContext()));
		LOGGER.info("Election event context successfully saved in vote verification. [electionEventId: {}]", electionEventId);

		// save election event context in control components
		List<ElectionContextResponsePayload> electionContextResponsePayloads = RetrofitConsumer.processResponse(
				messageBrokerOrchestratorClient.saveElectionEventContext(electionEventId, electionEventContextPayload));
		LOGGER.info("Election event context successfully saved in control components. [electionEventId: {}]", electionEventId);

		return electionContextResponsePayloads;
	}

	/**
	 * Retrieves the election event context for a given election event id.
	 *
	 * @param electionEventId the election event id.
	 * @return the election event context.
	 * @throws ResourceNotFoundException if the election event context does not exist for the given id.
	 */
	public ElectionEventContextEntity retrieveElectionEventContext(final String electionEventId) throws ResourceNotFoundException {
		validateUUID(electionEventId);

		// retrieve the election event context
		return electionEventContextRepository.findByElectionEventId(electionEventId);
	}

	private ElectionEventContextEntity getElectionEventContextEntity(final ElectionEventContext electionEventContext) {
		final ElectionEventContextEntity electionEventContextEntity = new ElectionEventContextEntity();
		electionEventContextEntity.setElectionEventId(electionEventContext.getElectionEventId());
		electionEventContextEntity.setStartTime(electionEventContext.getStartTime());
		electionEventContextEntity.setFinishTime(electionEventContext.getFinishTime());

		byte[] serializedCombinedControlComponentPublicKeys;
		byte[] serializedElectoralBoardPublicKey;
		byte[] serializedElectionPublicKey;
		byte[] serializedChoiceReturnCodesPublicKey;
		try {
			serializedCombinedControlComponentPublicKeys = objectMapper.writeValueAsBytes(
					electionEventContext.getCombinedControlComponentPublicKeys());
			serializedElectoralBoardPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getElectoralBoardPublicKey());
			serializedElectionPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getElectionPublicKey());
			serializedChoiceReturnCodesPublicKey = objectMapper.writeValueAsBytes(electionEventContext.getChoiceReturnCodesEncryptionPublicKey());

		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize election event context.", e);
		}
		electionEventContextEntity.setCombinedControlComponentPublicKeys(serializedCombinedControlComponentPublicKeys);
		electionEventContextEntity.setElectoralBoardPublicKey(serializedElectoralBoardPublicKey);
		electionEventContextEntity.setElectionPublicKey(serializedElectionPublicKey);
		electionEventContextEntity.setChoiceReturnCodesEncryptionPublicKey(serializedChoiceReturnCodesPublicKey);

		return electionEventContextEntity;
	}
}
