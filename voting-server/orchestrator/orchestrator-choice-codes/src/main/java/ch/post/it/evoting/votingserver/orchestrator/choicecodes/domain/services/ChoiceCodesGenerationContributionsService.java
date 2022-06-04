/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Queue;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ChoiceCodesComputationStatus;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.model.computedvalues.ComputedValuesRepository;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.QueuesConfig;

public class ChoiceCodesGenerationContributionsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChoiceCodesGenerationContributionsService.class);

	@Inject
	private MessagingService messagingService;

	@Inject
	@ChoiceCodesGeneration
	private MessageListener contributionsConsumer;

	@Inject
	private ComputedValuesService choiceCodesService;

	@Inject
	private ComputedValuesRepository computedValuesRepository;

	@Inject
	private ObjectMapper objectMapper;

	public void request(String trackingId, ReturnCodeGenerationRequestPayload payload) throws DuplicateEntryException, ResourceNotFoundException {

		LOGGER.info("OR:{} - Requesting the choice codes compute contributions for generation phase", trackingId);

		String tenantId = payload.getTenantId();
		String electionEventId = payload.getElectionEventId();
		String verificationCardSetId = payload.getVerificationCardSetId();
		int chunkId = payload.getChunkId();

		boolean existsComputedValues = computedValuesRepository
				.existsByTenantIdElectionEventIdVerificationCardSetIdChunkId(tenantId, electionEventId, verificationCardSetId, chunkId);

		if (existsComputedValues) {
			throw new DuplicateEntryException(
					"Choice codes computations already exist for the tenant: " + tenantId + " election event " + electionEventId
							+ " verification card set id " + verificationCardSetId + " chunk id " + chunkId);
		}

		choiceCodesService.create(tenantId, electionEventId, verificationCardSetId, chunkId);

		ChoiceCodeGenerationDTO<ReturnCodeGenerationRequestPayload> dto = new ChoiceCodeGenerationDTO<>(UUID.randomUUID(), trackingId, payload);

		publishPartialCodesToReqQ(dto);
	}

	private void publishPartialCodesToReqQ(final ChoiceCodeGenerationDTO<ReturnCodeGenerationRequestPayload> choiceCodeGenerationDTO)
			throws ResourceNotFoundException {

		final Queue[] queues = QueuesConfig.GENERATION_CONTRIBUTIONS_REQ_QUEUES;
		if (queues.length == 0) {
			throw new IllegalStateException("No choice codes compute contributions request queues provided to publish to.");
		}

		// Send dto as bytes because of MessagingService.
		final byte[] choiceCodeGenerationBytes;
		try {
			choiceCodeGenerationBytes = objectMapper.writeValueAsString(choiceCodeGenerationDTO).getBytes(StandardCharsets.UTF_8);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize choice code generation dto.", e);
		}

		for (Queue queue : queues) {
			try {
				messagingService.send(queue, choiceCodeGenerationBytes);
			} catch (MessagingException e) {
				throw new ResourceNotFoundException("Error publishing partial codes to the nodes queues ", e);
			}
		}
	}

	public void writeToStream(OutputStream stream, String tenantId, String electionEventId, String verificationCardSetId, int chunkId)
			throws IOException, ResourceNotFoundException {

		LOGGER.info("OR - Retrieving the choice codes compute contributions for generation phase");
		computedValuesRepository
				.writeJsonToStreamForTenantIdElectionEventIdVerificationCardSetIdChunkId(stream, tenantId, electionEventId, verificationCardSetId,
						chunkId);
	}

	/**
	 * Check the status of choice code generation values.
	 *
	 * @throws ResourceNotFoundException
	 */
	public ChoiceCodesComputationStatus getComputedValuesStatus(String electionEventId, String tenantId, String verificationCardSetId, int chunkId)
			throws ResourceNotFoundException {

		boolean exist = computedValuesRepository
				.existsByTenantIdElectionEventIdVerificationCardSetIdChunkId(tenantId, electionEventId, verificationCardSetId, chunkId);

		if (!exist) {
			throw new ResourceNotFoundException("Did not find any record in the relevant table matching the specified parameters");
		}

		boolean computed = computedValuesRepository
				.isComputedByTenantIdElectionEventIdVerificationCardSetIdChunkId(tenantId, electionEventId, verificationCardSetId, chunkId);

		return computed ? ChoiceCodesComputationStatus.COMPUTED : ChoiceCodesComputationStatus.COMPUTING;
	}

	/**
	 * Check the status of choice code generation values.
	 *
	 * @throws ResourceNotFoundException
	 */
	public ChoiceCodesComputationStatus getCompositeComputedValuesStatus(String electionEventId, String tenantId, String verificationCardSetId,
			int chunkCount) throws ResourceNotFoundException {

		boolean exist = computedValuesRepository
				.existsByTenantIdElectionEventIdVerificationCardSetId(tenantId, electionEventId, verificationCardSetId);

		if (!exist) {
			throw new ResourceNotFoundException("Did not find any record in the relevant table matching the specified parameters");
		}

		boolean computed = computedValuesRepository
				.areComputedByTenantIdElectionEventIdVerificationCardSetId(tenantId, electionEventId, verificationCardSetId, chunkCount);

		return computed ? ChoiceCodesComputationStatus.COMPUTED : ChoiceCodesComputationStatus.COMPUTING;
	}

	public void startup() throws MessagingException {

		LOGGER.info("OR - Consuming the choice codes compute contributions queues");

		Queue[] queues = QueuesConfig.GENERATION_CONTRIBUTIONS_RES_QUEUES;

		if (queues.length == 0) {
			throw new IllegalStateException("No choice codes compute contributions response queues provided to listen to.");
		}

		for (Queue queue : queues) {
			messagingService.createReceiver(queue, contributionsConsumer);
		}
	}

	public void shutdown() throws MessagingException {

		LOGGER.info("OR - Disconnecting from the choice codes compute contributions queues");

		Queue[] queues = QueuesConfig.GENERATION_CONTRIBUTIONS_RES_QUEUES;

		if (queues.length == 0) {
			throw new IllegalStateException("No choice codes compute contributions response queues provided to listen to.");
		}

		for (Queue queue : queues) {
			messagingService.destroyReceiver(queue, contributionsConsumer);
		}
	}

}
