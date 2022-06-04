/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;
import java.io.OutputStream;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

/**
 * Interface for VotingCardStateService.
 */
public interface VotingCardStateService {

	/**
	 * Gets the voting card state. If it is not found, returns a new instance of voting card state
	 * with state set to EMPTY.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @return the voting card state.
	 * @throws ApplicationException when any of the input parameters is empty or null.
	 */
	VotingCardState getVotingCardState(String tenantId, String electionEventId, String votingCardId) throws ApplicationException;

	/**
	 * Update the voting card state with the new state.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @param state           - the new state of the voting card.
	 * @return the voting card state.
	 * @throws ApplicationException      when any of the input parameters is empty or null.
	 * @throws ResourceNotFoundException when voting card state is not found.
	 * @throws DuplicateEntryException
	 */
	void updateVotingCardState(String tenantId, String electionEventId, String votingCardId, VotingCardStates state)
			throws ResourceNotFoundException, ApplicationException, DuplicateEntryException;

	/**
	 * Block voting card and ignore unable.
	 *
	 * @param tenantId        - the tenant id.
	 * @param electionEventId - the election event id.
	 * @param votingCardId    - the voting card id.
	 * @throws ApplicationException      when any of the input parameters is empty or null.
	 * @throws ResourceNotFoundException when voting card state is not found.
	 * @throws DuplicateEntryException
	 */
	void blockVotingCardIgnoreUnable(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException, ApplicationException, DuplicateEntryException;

	/**
	 * Initializes and stores a voting card state.
	 *
	 * @param votingCardState the voting card state object to be stored.
	 * @throws EntryPersistenceException
	 * @throws DuplicateEntryException
	 */
	void initializeVotingCardState(VotingCardState votingCardState)
			throws DuplicateEntryException, EntryPersistenceException, ResourceNotFoundException;

	/**
	 * Increments the number of attempts of the specified voting card state.
	 *
	 * @param tenantId         - the tenant id.
	 * @param electionEventId- the election event id.
	 * @param votingCardId-    the voting card id.
	 * @throws ApplicationException      when any of the input parameters is empty or null.
	 * @throws ResourceNotFoundException when voting card state is not found.
	 * @throws DuplicateEntryException
	 */
	void incrementVotingCardAttempts(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException, ApplicationException, DuplicateEntryException;

	/**
	 * Writes identifier and state of inactive voting cards into a given stream using CSV format.
	 *
	 * @param tenantId        the tenant identifier
	 * @param electionEventId the election event identifier
	 * @param stream          the stream
	 * @throws IOException I/O error occurred.
	 */
	void writeIdAndStateOfInactiveVotingCards(String tenantId, String electionEventId, OutputStream stream) throws IOException;

}
