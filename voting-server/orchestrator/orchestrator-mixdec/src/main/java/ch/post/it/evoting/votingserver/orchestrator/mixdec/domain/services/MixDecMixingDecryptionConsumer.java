/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetState;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecPayloadRepository;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence.MixDecPayloadRepositoryException;

public class MixDecMixingDecryptionConsumer implements MessageListener {

	@Inject
	private Logger logger;

	@Inject
	private MixDecPayloadRepository mixDecPayloadRepository;

	@Inject
	private MixDecBallotBoxService mixDecBallotBoxService;

	@Inject
	private ObjectMapper mapper;

	/**
	 * Receives the result of mixing and decrypting in one node. If the DTO has been through all the nodes already, the process is finished.
	 * Otherwise, the resulting DTO must be sent to one of the nodes that have not yet visited.
	 *
	 * @param message the output mixing DTO that has been processed by a node.
	 */
	@Override
	public void onMessage(final Object message) {
		final byte[] messageBytes = (byte[]) message;

		final MixnetState mixnetState;
		try {
			mixnetState = mapper.readValue(messageBytes, MixnetState.class);
		} catch (IOException e) {
			logger.error("Failed to deserialize received message into a MixnetState.", e);
			return;
		}

		final int lastVisitedNode = mixnetState.getNodeToVisit();
		final String electionEventId = mixnetState.getPayload().getElectionEventId();
		final String ballotBoxId = mixnetState.getPayload().getBallotBoxId();

		logger.info("Received MixnetState from node. [electionEventId: {}, ballotBoxId: {}, node {}]", electionEventId, ballotBoxId, lastVisitedNode);

		// Check whether the DTO is reporting an error.
		if (mixnetState.getMixnetError() == null) {
			logger.debug("Processing ballot box... [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);

			try {
				// Persist the received MixnetState.
				persistPartialResults(mixnetState);

				// Check the index of the last visited node.
				if (lastVisitedNode == 3) {
					// All nodes have been visited. Store the final results.
					persistFinalResults(mixnetState);
					logger.info("All nodes have been visited, final result persisted. [electionEventId: {}, ballotBoxId: {}]", electionEventId,
							ballotBoxId);
				} else {
					// If the MixnetState has not yet been through all nodes, send it to the next one.
					mixnetState.incrementNodeToVisit();
					mixDecBallotBoxService.sendMessage(mixnetState);
				}
			} catch (ApplicationException e) {
				logger.error("Error mixing payload. [electionEventId: {}, ballotBoxId: {}].", electionEventId, ballotBoxId, e);
			} catch (MixDecPayloadRepositoryException e) {
				logger.error("Mixing payload could not be stored properly. [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId, e);
			}
		} else {
			logger.warn("MixnetState payload processing failed. [electionEventId: {}, ballotBoxId: {} error: {}]", electionEventId, ballotBoxId,
					mixnetState.getMixnetError());

			mixDecBallotBoxService.updateErrorBallotBoxStatus(electionEventId, ballotBoxId, mixnetState.getMixnetError());
			try {
				if (mixnetState.getRetryCount() > 0) {
					mixnetState.decrementRetryCount();
					mixDecBallotBoxService.sendMessage(mixnetState);
				} else {
					// No retries left.
					logger.error("Processing payload will not be further attempted. [electionEventId: {}, ballotBoxId: {}, error: {}]",
							electionEventId, ballotBoxId, mixnetState.getMixnetError());
				}
			} catch (ApplicationException e) {
				logger.error("Error mixing payload of ballot box [electionEventId: {}, ballotBoxId: {}].", electionEventId, ballotBoxId, e);
			}
		}

	}

	/**
	 * Stores the results of having mixed and decrypted a ballot box in one node.
	 *
	 * @param mixnetState the data as coming out from an online mixing node.
	 */
	private void persistPartialResults(final MixnetState mixnetState) throws MixDecPayloadRepositoryException {
		final String electionEventId = mixnetState.getPayload().getElectionEventId();
		final String ballotBoxId = mixnetState.getPayload().getBallotBoxId();
		final int nodeToVisit = mixnetState.getNodeToVisit();

		logger.debug("Storing mixing and decryption results from node... [electionEventId: {}, ballotBoxId: {}, node {}]", electionEventId,
				ballotBoxId, nodeToVisit);
		try {
			mixDecPayloadRepository.save(mixnetState, false);
			logger.info("Mixing and decryption results from node have been stored. [electionEventId: {}, ballotBoxId: {}, node {}]", electionEventId,
					ballotBoxId, nodeToVisit);
		} catch (DuplicateEntryException e) {
			logger.warn("Node output is already stored. [electionEventId: {}, ballotBoxId: {}, node: {}]", electionEventId, ballotBoxId, nodeToVisit);
		}
	}

	/**
	 * Stores the results of having fully mixed and decrypted a vote set.
	 *
	 * @param mixnetState the data as coming out from the last online mixing node.
	 */
	private void persistFinalResults(final MixnetState mixnetState) {
		final String electionEventId = mixnetState.getPayload().getElectionEventId();
		final String ballotBoxId = mixnetState.getPayload().getBallotBoxId();

		logger.debug("Storing final mixing and decryption results... [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);

		mixDecBallotBoxService.updateProcessedBallotBoxStatus(electionEventId, ballotBoxId);
		logger.info("Ballot box has been fully processed and persisted. [electionEventId: {}, ballotBoxId: {}]", electionEventId, ballotBoxId);
	}

}
