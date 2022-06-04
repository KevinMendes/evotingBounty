/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import static ch.post.it.evoting.domain.SharedQueue.MIX_DEC_ONLINE_REQUEST_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetState;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxIdImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.Queue;
import ch.post.it.evoting.votingserver.orchestrator.commons.config.QueuesConfig;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.BallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.CleansedBallotBoxRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecBallotBoxStatusRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecPayloadRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecPayloadRepositoryException;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ResourceNotReadyException;

@Stateless
public class MixDecBallotBoxService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecBallotBoxService.class);
	private static final String TENANT_ID = "100";

	@Inject
	private MixDecBallotBoxStatusRepository mixDecBallotBoxStatusRepository;

	@Inject
	private MixDecPayloadRepository nodeOutputRepository;

	@Inject
	private CleansedBallotBoxRepository cleansedBallotBoxRepository;

	@Inject
	private MessagingService messagingService;

	@Inject
	private MixDecMixingDecryptionConsumer mixDecMixingDecryptionConsumer;

	@Inject
	private ObjectMapper mapper;

	public void startup() throws MessagingException {
		LOGGER.info("OR - Setting up consumers for the mixing and decryption queues...");
		Queue[] queues = QueuesConfig.MIX_DEC_COMPUTATION_RES_QUEUES;

		if (queues == null || queues.length == 0) {
			throw new IllegalStateException("No mixing+decryption response queues provided to listen to.");
		}

		try {
			for (Queue queue : queues) {
				messagingService.createReceiver(queue, mixDecMixingDecryptionConsumer);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Mixing and decryption queue consumer for {} is up and running", queue.name());
				}
			}
		} catch (MessagingException e) {
			throw e;
		} catch (Exception e) {
			throw new MessagingException(e);
		}

		LOGGER.info("OR - Mixing and decryption queue consumers set up for {}", (Object[]) queues);
	}

	public void shutdown() throws MessagingException {
		LOGGER.info("OR - Tearing down consumers for the mixing and decryption queues...");
		Queue[] queues = QueuesConfig.MIX_DEC_COMPUTATION_RES_QUEUES;

		if (queues == null || queues.length == 0) {
			throw new IllegalStateException("No mixing+decryption response queues provided to listen to.");
		}

		for (Queue queue : queues) {
			messagingService.destroyReceiver(queue, mixDecMixingDecryptionConsumer);
		}

		LOGGER.info("OR - Mixing and decryption queue consumers torn down for {}", (Object[]) queues);
	}

	public List<BallotBoxStatus> processBallotBoxes(final String electionEventId, final List<String> ballotBoxIds, final String trackingId) {
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxIds);
		ballotBoxIds.forEach(UUIDValidations::validateUUID);
		checkNotNull(trackingId);

		List<BallotBoxStatus> ballotBoxStatuses = new ArrayList<>();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Received list of ballot boxes for mixing and decrypting: {}", ballotBoxIds);
		}

		for (String ballotBoxId : ballotBoxIds) {

			if (isBallotBoxMixingFinished(electionEventId, ballotBoxId)) {
				BallotBoxStatus ballotBoxStatus = new BallotBoxStatus();
				ballotBoxStatus.setBallotBoxId(ballotBoxId);
				ballotBoxStatus.setProcessStatus(MixDecStatus.ERROR);
				ballotBoxStatus.setErrorMessage("Ballot box mixing has already started");
				ballotBoxStatuses.add(ballotBoxStatus);
				continue;
			}

			BallotBoxStatus ballotBoxStatus = processBallotBox(electionEventId, ballotBoxId);
			ballotBoxStatuses.add(ballotBoxStatus);
		}

		return ballotBoxStatuses;
	}

	private BallotBoxStatus processBallotBox(final String electionEventId, final String ballotBoxId) {
		LOGGER.info("Ballot box {} is about to be processed", ballotBoxId);

		BallotBoxStatus ballotBoxStatus = new BallotBoxStatus();
		ballotBoxStatus.setBallotBoxId(ballotBoxId);

		try {
			LOGGER.info("Starting processing of ballot box {}", ballotBoxId);
			prepareAndSendMixnetState(electionEventId, ballotBoxId);

			if (0 != mixDecBallotBoxStatusRepository.countByMixDecBallotBoxStatus(electionEventId, ballotBoxId, MixDecStatus.ERROR.toString())) {
				final String errorMessage = "Error sending ballot box " + ballotBoxId + " for mixing.";
				LOGGER.error("Error processing ballot box {}", errorMessage);
				ballotBoxStatus.setProcessStatus(MixDecStatus.ERROR);
				ballotBoxStatus.setErrorMessage(errorMessage);
			} else {
				ballotBoxStatus.setProcessStatus(MixDecStatus.PROCESSING);
			}

		} catch (ResourceNotReadyException e) {
			LOGGER.info("Ballot box not ready for mixdecrypt", e);
			ballotBoxStatus.setProcessStatus(MixDecStatus.NOT_CLOSED);
		} catch (ResourceNotFoundException e) {
			LOGGER.warn("Could not find requested ballot box", e);
			ballotBoxStatus.setProcessStatus(MixDecStatus.NOT_FOUND);
		} catch (Exception e) {
			LOGGER.error("Error processing ballotbox", e);
			ballotBoxStatus.setProcessStatus(MixDecStatus.ERROR);
			ballotBoxStatus.setErrorMessage(e.toString());
		}

		LOGGER.info("Ballot box {} status is now {} ", ballotBoxStatus.getBallotBoxId(), ballotBoxStatus.getProcessStatus());

		return ballotBoxStatus;
	}

	private void saveStatus(final String electionEventId, final String ballotBoxId, final MixDecStatus ballotBoxStatus, final String errorMessage)
			throws ApplicationException {

		try {
			mixDecBallotBoxStatusRepository.save(electionEventId, ballotBoxId, ballotBoxStatus.toString(), errorMessage);
		} catch (EntryPersistenceException e) {
			throw new ApplicationException(String.format("Status could not be saved for ballot box %s-%s", electionEventId, ballotBoxId), e);
		}
	}

	private boolean isBallotBoxMixingFinished(final String electionEventId, final String ballotBoxId) {
		return MixDecStatus.MIXED.equals(getMixDecBallotBoxStatus(electionEventId, ballotBoxId).getProcessStatus());
	}

	public void sendMessage(final MixnetState mixnetState) throws ApplicationException {
		// Send the mixing data to a node.
		final int nodeId = mixnetState.getNodeToVisit();

		final Queue partialMixingDecryptQueue = new Queue(MIX_DEC_ONLINE_REQUEST_PATTERN + nodeId);

		LOGGER.info("Sending MixnetState for mixing... [electionEventId: {}, ballotBoxId: {}, queue {}]",
				mixnetState.getPayload().getElectionEventId(), mixnetState.getPayload().getBallotBoxId(), partialMixingDecryptQueue);
		try {
			final String mixnetStateJson = mapper.writeValueAsString(mixnetState);
			messagingService.send(partialMixingDecryptQueue, mixnetStateJson.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new ApplicationException(
					String.format("Failed to send MixnetState for mixing. [electionEventId: %s, ballotBoxId: %s, queue %s].",
							mixnetState.getPayload().getElectionEventId(), mixnetState.getPayload().getBallotBoxId(), partialMixingDecryptQueue), e);
		}
	}

	private void prepareAndSendMixnetState(final String electionEventId, final String ballotBoxId)
			throws ResourceNotReadyException, ResourceNotFoundException, IOException {

		final BallotBoxId bbid = new BallotBoxIdImpl(TENANT_ID, electionEventId, ballotBoxId);

		try {
			saveStatus(electionEventId, ballotBoxId, MixDecStatus.PROCESSING, null);

			// If the ballot box does not contain vote, it is not necessary to mix and decrypt it.
			if (cleansedBallotBoxRepository.isBallotBoxEmpty(electionEventId, ballotBoxId)) {
				updateStatus(electionEventId, ballotBoxId, MixDecStatus.MIXED, null);
				return;
			}

			// Get the mixnet initial payload to send to first mixing control component.
			final MixnetInitialPayload mixnetInitialPayload = cleansedBallotBoxRepository.getMixnetInitialPayload(bbid);

			// Wrap payload in the MixnetState.
			final MixnetState mixnetState = new MixnetState(mixnetInitialPayload);

			persistMixDecInput(mixnetState);

			// Increment node number to visit and send.
			mixnetState.incrementNodeToVisit();
			sendMessage(mixnetState);
		} catch (ApplicationException e) {
			updateStatus(electionEventId, ballotBoxId, MixDecStatus.ERROR, e.toString());
			LOGGER.error("Error mixing payload. [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId, e);
		}
	}

	private void persistMixDecInput(MixnetState mixnetState) {
		final String electionEventId = mixnetState.getPayload().getElectionEventId();
		final String ballotBoxId = mixnetState.getPayload().getBallotBoxId();
		LOGGER.debug("Storing initial payload... [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);

		try {
			nodeOutputRepository.save(mixnetState, true);
			LOGGER.info("Initial payload has been stored. [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);
		} catch (DuplicateEntryException | MixDecPayloadRepositoryException e) {
			LOGGER.warn("Initial payload is already stored. [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);
		}
	}

	public void updateProcessedBallotBoxStatus(final String electionEventId, final String ballotBoxId) {
		updateStatus(electionEventId, ballotBoxId, MixDecStatus.MIXED);
	}

	public void updateErrorBallotBoxStatus(final String electionEventId, final String ballotBoxId, final String errorMessage) {
		updateStatus(electionEventId, ballotBoxId, MixDecStatus.ERROR, errorMessage);
	}

	private void updateStatus(final String electionEventId, final String ballotBoxId, final MixDecStatus ballotBoxStatus) {
		updateStatus(electionEventId, ballotBoxId, ballotBoxStatus, null);
	}

	private void updateStatus(final String electionEventId, final String ballotBoxId, final MixDecStatus ballotBoxStatus, final String errorMessage) {
		try {
			mixDecBallotBoxStatusRepository.update(electionEventId, ballotBoxId, ballotBoxStatus.toString(), errorMessage);
		} catch (ResourceNotFoundException | EntryPersistenceException e) {
			throw new IllegalStateException("Failed to update Ballot Box mixing status", e);
		}
	}

	public List<BallotBoxStatus> getMixDecBallotBoxStatus(final String electionEventId, final String[] ballotBoxIds) {

		List<BallotBoxStatus> ballotBoxStatusList = new ArrayList<>();

		for (String ballotBoxId : ballotBoxIds) {
			BallotBoxStatus ballotBoxStatus = getMixDecBallotBoxStatus(electionEventId, ballotBoxId);
			ballotBoxStatusList.add(ballotBoxStatus);
		}

		return ballotBoxStatusList;
	}

	public BallotBoxStatus getMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId) {
		BallotBoxStatus ballotBoxStatus = new BallotBoxStatus();
		ballotBoxStatus.setBallotBoxId(ballotBoxId);

		long mixDecBallotBoxChunkStatusCount = mixDecBallotBoxStatusRepository.countByMixDecBallotBoxStatus(electionEventId, ballotBoxId);
		long mixDecBallotBoxChunkStatusMixedCount = mixDecBallotBoxStatusRepository
				.countByMixDecBallotBoxStatus(electionEventId, ballotBoxId, MixDecStatus.MIXED.toString());
		long mixDecBallotBoxChunkStatusErrorCount = mixDecBallotBoxStatusRepository
				.countByMixDecBallotBoxStatus(electionEventId, ballotBoxId, MixDecStatus.ERROR.toString());

		if (mixDecBallotBoxChunkStatusCount == 0) {
			ballotBoxStatus.setProcessStatus(MixDecStatus.NOT_FOUND);
		} else if (mixDecBallotBoxChunkStatusErrorCount > 0) {
			ballotBoxStatus.setProcessStatus(MixDecStatus.ERROR);
		} else if (mixDecBallotBoxChunkStatusMixedCount < mixDecBallotBoxChunkStatusCount) {
			ballotBoxStatus.setProcessStatus(MixDecStatus.PROCESSING);
		} else if (mixDecBallotBoxChunkStatusMixedCount == mixDecBallotBoxChunkStatusCount) {
			ballotBoxStatus.setProcessStatus(MixDecStatus.MIXED);
		}
		return ballotBoxStatus;
	}

	public List<MixnetPayload> getBallotBoxPayloads(BallotBoxId ballotBoxId, boolean getInitial) {
		return nodeOutputRepository.getBallotBoxPayloadList(ballotBoxId.getElectionEventId(), ballotBoxId.getId(), getInitial);
	}
}
