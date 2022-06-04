/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.ejb.EJBException;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

/**
 * Decorator for voting card state service.
 */
@Decorator
public abstract class VotingCardStateServiceDecorator implements VotingCardStateService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardStateServiceDecorator.class);

	@Inject
	@Delegate
	private VotingCardStateService votingCardStateService;

	/**
	 * @see VotingCardStateService#getVotingCardState(String, String, String)
	 */
	@Override
	public VotingCardState getVotingCardState(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {
		try {
			VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
			LOGGER.info("Voting card state found. [tenantId: {}, electionEventId: {}, votingCardId: {}, state: {}]", tenantId, electionEventId,
					votingCardId, result.getState());
			return result;
		} catch (EJBException | ApplicationException e) {
			LOGGER.info("Voting card state not found. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId,
					votingCardId);
			throw e;
		}
	}

	/**
	 * @see VotingCardStateService#updateVotingCardState(String, String, String, VotingCardStates)
	 */
	@Override
	public void updateVotingCardState(String tenantId, String electionEventId, String votingCardId, VotingCardStates state)
			throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		votingCardStateService.updateVotingCardState(tenantId, electionEventId, votingCardId, state);
	}

}
