/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;

import org.apache.commons.io.output.CloseShieldOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.common.csv.ExportedPartialVotingCardStateItemWriter;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateEvaluator;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

/**
 * Service for handling voting card state.
 */
@Stateless
public class VotingCardStateServiceImpl implements VotingCardStateService {

	private static final String TENANT_ID = "tenantId";
	private static final String ELECTION_EVENT_ID = "electionEventId";
	private static final String VOTING_CARD_ID = "votingCardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardStateServiceImpl.class);

	@EJB
	private VotingCardStateRepository votingCardStateRepository;

	@EJB
	private VoterInformationRepository voterInformationRepository;

	@EJB
	private VoteRepository voteRepository;

	@EJB
	private VoteCastCodeRepository voteCastCodeRepository;

	/**
	 * @see VotingCardStateService#initializeVotingCardState(VotingCardState)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void initializeVotingCardState(VotingCardState votingCardState) throws DuplicateEntryException {
		if (votingCardState.getState().equals(VotingCardStates.NONE)) {
			votingCardState.setState(VotingCardStates.NOT_SENT);
		}

		final Optional<VotingCardState> votingCardState1 = votingCardStateRepository
				.acquire(votingCardState.getTenantId(), votingCardState.getElectionEventId(), votingCardState.getVotingCardId());
		// persist just in case that the voting card state is not already
		// persisted
		if (!votingCardState1.isPresent()) {
			votingCardStateRepository.save(votingCardState);
		}

	}

	/**
	 * @see VotingCardStateService#getVotingCardState(String, String, String)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public VotingCardState getVotingCardState(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {

		validateParameter(tenantId, TENANT_ID);
		validateParameter(electionEventId, ELECTION_EVENT_ID);
		validateParameter(votingCardId, VOTING_CARD_ID);

		// recover voting card state
		Optional<VotingCardState> votingCardState;
		try {
			votingCardState = votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId);
		} catch (PessimisticLockException | LockTimeoutException e) {
			throw new ApplicationException(
					String.format("Cannot create lock on entity with id: %s with tenant %s and electionEvent %s", votingCardId, tenantId,
							electionEventId), e);
		}

		if (!votingCardState.isPresent()) {
			VotingCardState state = new VotingCardState();
			state.setTenantId(tenantId);
			state.setElectionEventId(electionEventId);
			state.setVotingCardId(votingCardId);
			state.setState(VotingCardStates.NONE);
			votingCardState = Optional.of(state);
		} else {
			final VotingCardStates currentState = votingCardState.get().getState();
			final VotingCardStates realState = getConsistentVotingCardState(tenantId, electionEventId, votingCardId, currentState);
			votingCardState.get().setState(realState);
		}

		return votingCardState.get();
	}

	/**
	 * @see VotingCardStateService#updateVotingCardState(String, String, String, VotingCardStates)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateVotingCardState(String tenantId, String electionEventId, String votingCardId, VotingCardStates state)
			throws ApplicationException, DuplicateEntryException {

		validateParameter(tenantId, TENANT_ID);
		validateParameter(electionEventId, ELECTION_EVENT_ID);
		validateParameter(votingCardId, VOTING_CARD_ID);
		validateParameter(state, "votingCardState");

		// get the voting card state
		Optional<VotingCardState> optionalOfVotingCardState = votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId);

		if (optionalOfVotingCardState.isPresent()) {
			// update state
			VotingCardState votingCardState = optionalOfVotingCardState.get();
			votingCardState.setState(state);

			votingCardStateRepository.save(votingCardState);

			LOGGER.info("Updating voting card state found. [tenantId: {}, electionEventId: {}, votingCardId: {}, state: {}]", tenantId,
					electionEventId, votingCardId, votingCardState.getState().name());
		} else {
			LOGGER.warn("Voting card state not found. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId,
					votingCardId);
		}
	}

	/**
	 * @see VotingCardStateService#updateVotingCardState(String, String, String, VotingCardStates)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void blockVotingCardIgnoreUnable(String tenantId, String electionEventId, String votingCardId)
			throws ApplicationException, DuplicateEntryException {

		validateParameter(tenantId, TENANT_ID);
		validateParameter(electionEventId, ELECTION_EVENT_ID);
		validateParameter(votingCardId, VOTING_CARD_ID);

		final VoterInformation voterInformation = getVoterInformation(tenantId, electionEventId, votingCardId);
		if (voterInformation == null) {
			return;
		}

		// get the voting card state
		VotingCardState votingCardState;
		try {
			final Optional<VotingCardState> current = votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId);
			if (!current.isPresent()) {
				votingCardState = new VotingCardState();
				votingCardState.setTenantId(tenantId);
				votingCardState.setElectionEventId(electionEventId);
				votingCardState.setVotingCardId(votingCardId);
				votingCardState.setState(VotingCardStates.NONE);
			} else {
				votingCardState = current.get();
			}

		} catch (EJBException e) {
			LOGGER.error("Failed to acquire VotingCardState.", e);
			return;
		}

		VotingCardStateEvaluator stateEvaluator = VotingCardStateEvaluator.forState(votingCardState.getState());
		if (stateEvaluator.canBeBlocked()) {
			// update state
			votingCardState.setState(VotingCardStates.BLOCKED);
			votingCardStateRepository.save(votingCardState);
			LOGGER.info("Blocking voting car. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId, votingCardId);
		} else {
			LOGGER.info("Invalid state when blocking voting card. [tenantId: {}, electionEventId: {}, votingCardId: {}, state: {}]", tenantId,
					electionEventId, votingCardId, votingCardState.getState().name());
		}
	}

	/**
	 * @see VotingCardStateService#incrementVotingCardAttempts(String, String, String)
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void incrementVotingCardAttempts(String tenantId, String electionEventId, String votingCardId)
			throws ApplicationException, DuplicateEntryException {

		validateParameter(tenantId, TENANT_ID);
		validateParameter(electionEventId, ELECTION_EVENT_ID);
		validateParameter(votingCardId, VOTING_CARD_ID);

		// get the voting card state
		Optional<VotingCardState> optional = votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId);

		if (optional.isPresent()) {
			// update state
			VotingCardState state = optional.get();
			state.incrementAttempts();
			votingCardStateRepository.save(state);

			LOGGER.info("Updating voting card state found. [tenantId: {}, electionEventId: {}, votingCardId: {}, state: {}]", tenantId,
					electionEventId, votingCardId, state.getState().name());
		}
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void writeIdAndStateOfInactiveVotingCards(final String tenantId, final String electionEventId, final OutputStream stream)
			throws IOException {
		try (ExportedPartialVotingCardStateItemWriter writer = new ExportedPartialVotingCardStateItemWriter(new CloseShieldOutputStream(stream))) {
			votingCardStateRepository.findAndWriteVotingCardsWithInactiveState(tenantId, electionEventId, writer);
		}
	}

	public VotingCardStates getConsistentVotingCardState(final String tenantId, final String electionEventId, final String votingCardId,
			final VotingCardStates votingCardState) {

		// init with the 'current' state. we only change it if there is a
		// consistency mismatch
		VotingCardStates realVotingCardState = votingCardState;
		VotingCardStateEvaluator stateEvaluator = VotingCardStateEvaluator.forState(votingCardState);

		if (stateEvaluator.votingCardStateRequiresConsistencyCheck()) {
			boolean voteCastCodeExists;
			try {
				voteCastCodeExists = checkVoteCastCodeExists(tenantId, electionEventId, votingCardId);
				if (voteCastCodeExists) {
					realVotingCardState = VotingCardStates.CAST;
					LOGGER.info("There is a Vote Cast Code for this VotingCard [tenant=%s, election=%s, votingCard=%s]. " + "Voting Card State '{}'",
							realVotingCardState.name());
				} else {
					// no vote cast code. check if a vote was sent
					final boolean voteExists = checkVoteExists(tenantId, electionEventId, votingCardId);
					if (voteExists) {
						realVotingCardState = VotingCardStates.SENT_BUT_NOT_CAST;
						LOGGER.info("There is a Vote for this VotingCard [tenant=%s, election=%s, votingCard=%s]. " + "VotingCard state is '{}'",
								realVotingCardState.name());
					} else {
						realVotingCardState = VotingCardStates.NOT_SENT;
						LOGGER.info("There is no Vote for this VotingCard [tenant=%s, election=%s, votingCard=%s]." + " VotingCard state is '{}'",
								realVotingCardState.name());
					}
				}
			} catch (VoteCastCodeRepositoryException vcE) {
				LOGGER.error("Error trying to check vote cast code exits.", vcE);
				realVotingCardState = VotingCardStates.CHOICE_CODES_FAILED;
			} catch (VoteRepositoryException vrE) {
				LOGGER.error("Error trying to check vote exits.", vrE);
				realVotingCardState = VotingCardStates.CHOICE_CODES_FAILED;
			}
		}
		return realVotingCardState;
	}

	private boolean checkVoteCastCodeExists(final String tenantId, final String electionEventId, final String votingCardId)
			throws VoteCastCodeRepositoryException {
		return voteCastCodeRepository.voteCastCodeExists(tenantId, electionEventId, votingCardId);
	}

	private boolean checkVoteExists(final String tenantId, final String electionEventId, final String votingCardId) throws VoteRepositoryException {
		return voteRepository.voteExists(tenantId, electionEventId, votingCardId);
	}

	/**
	 * Check If Voting Card exists
	 */
	private VoterInformation getVoterInformation(final String tenantId, final String electionEventId, String vcId) {

		try {
			return voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, vcId);
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Voter information not found. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId, vcId, e);
			return null;
		}
	}

	private void validateParameter(Object value, String objectName) throws ApplicationException {
		if (value == null || "".equals(value)) {
			throw new ApplicationException(objectName + " is null or empty.");
		}
	}
}
