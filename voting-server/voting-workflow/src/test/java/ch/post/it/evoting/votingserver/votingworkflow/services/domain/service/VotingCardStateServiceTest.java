/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.Optional;

import javax.persistence.PessimisticLockException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateRepository;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

@RunWith(MockitoJUnitRunner.class)
public class VotingCardStateServiceTest {

	@InjectMocks
	VotingCardStateService votingCardStateService = new VotingCardStateServiceImpl();

	@Mock
	VotingCardStateRepository votingCardRepositoryMock;

	@Mock
	VoteCastCodeRepository voteCastCodeRepositoryMock;

	@Mock
	VoterInformationRepository voterInformationRepository;

	@Mock
	VoteRepository voteRepository;

	@Test
	public void getVotingCardStateNullInputParameters() {
		String tenantId = null;
		String electionEventId = "1";
		String votingCardId = "1";

		assertThrows(ApplicationException.class, () -> votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId));
	}

	@Test
	public void getVotingCardStateEmptyInputParameters() {
		String tenantId = "";
		String electionEventId = "1";
		String votingCardId = "1";

		assertThrows(ApplicationException.class, () -> votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId));
	}

	@Test
	public void getVotingCardStateNotFound() throws ApplicationException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.empty());

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertEquals(VotingCardStates.NONE, result.getState());
	}

	@Test(expected = ApplicationException.class)
	public void getVotingCardStateLockingException() throws ApplicationException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenThrow(new PessimisticLockException("exception"));

		votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
	}

	@Test
	public void getVotingCardState() throws ApplicationException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardState = new VotingCardState();
		votingCardState.setTenantId(tenantId);
		votingCardState.setElectionEventId(electionEventId);
		votingCardState.setVotingCardId(votingCardId);
		votingCardState.setState(VotingCardStates.NOT_SENT);
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardState));

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertNotNull(result);
	}

	@Test
	public void whenNotSentWithVoteShouldReturnSentNotCast() throws ApplicationException, VoteCastCodeRepositoryException, VoteRepositoryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voteCastCodeRepositoryMock.voteCastCodeExists(anyString(), anyString(), anyString())).thenReturn(false);
		when(voteRepository.voteExists(anyString(), anyString(), anyString())).thenReturn(true);
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(notSent(tenantId, electionEventId, votingCardId));

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertNotNull(result);
		assertEquals(VotingCardStates.SENT_BUT_NOT_CAST, result.getState());
	}

	@Test
	public void whenSentNotCastWithVoteShouldReturnSentNotCast()
			throws ApplicationException, VoteCastCodeRepositoryException, VoteRepositoryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voteCastCodeRepositoryMock.voteCastCodeExists(anyString(), anyString(), anyString())).thenReturn(false);
		when(voteRepository.voteExists(anyString(), anyString(), anyString())).thenReturn(true);
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId))
				.thenReturn(sentNotCast(tenantId, electionEventId, votingCardId));

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertNotNull(result);
		assertEquals(VotingCardStates.SENT_BUT_NOT_CAST, result.getState());
	}

	@Test
	public void whenNotSentWithVoteCastCodeShouldReturnCast() throws ApplicationException, VoteCastCodeRepositoryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voteCastCodeRepositoryMock.voteCastCodeExists(anyString(), anyString(), anyString())).thenReturn(true);

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(notSent(tenantId, electionEventId, votingCardId));

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertNotNull(result);
		assertEquals(VotingCardStates.CAST, result.getState());
	}

	@Test
	public void whenSentNotCastWithVoteCastCodeShouldReturnCast() throws ApplicationException, VoteCastCodeRepositoryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voteCastCodeRepositoryMock.voteCastCodeExists(anyString(), anyString(), anyString())).thenReturn(true);

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId))
				.thenReturn(sentNotCast(tenantId, electionEventId, votingCardId));

		VotingCardState result = votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId);
		assertNotNull(result);
		assertEquals(VotingCardStates.CAST, result.getState());
	}

	@Test
	public void blockVotingCardAllowedWhenStateIsSentButNotCast() throws Exception {
		// given
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(anyString(), anyString(), anyString()))
				.thenReturn(new VoterInformation());

		doReturn(sentNotCast(tenantId, electionEventId, votingCardId)).when(votingCardRepositoryMock).acquire(anyString(), anyString(), anyString());

		// when
		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);

		// then verify that we called the save method, which means it was allowed to block
		Mockito.verify(votingCardRepositoryMock, times(1)).save(any());
	}

	@Test
	public void blockVotingCardNotAllowedWhenStateIsCast() throws Exception {
		// given
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(anyString(), anyString(), anyString()))
				.thenReturn(new VoterInformation());

		doReturn(cast(tenantId, electionEventId, votingCardId)).when(votingCardRepositoryMock).acquire(anyString(), anyString(), anyString());

		// when
		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);

		// then verify that we DID NOT call the save method, which means it was NOT allowed to block
		Mockito.verify(votingCardRepositoryMock, times(0)).save(any());
	}

	@Test
	public void testUpdateAttemptsSuccessful() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.CAST);
		votingCardStateMock.setAttempts(0);

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardStateMock));

		votingCardStateService.incrementVotingCardAttempts(tenantId, electionEventId, votingCardId);
		assertEquals(1, votingCardStateMock.getAttempts());
	}

	@Test
	public void testBlockVotingCardIgnoreUnableSuccessful() throws Exception {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.NOT_SENT);
		votingCardStateMock.setAttempts(0);

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId))
				.thenReturn(new VoterInformation());
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardStateMock));

		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);

		assertEquals(VotingCardStates.BLOCKED, votingCardStateMock.getState());
	}

	@Test
	public void testBlockVotingCardIgnoreUnableNotPresentSuccessful() throws Exception {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId))
				.thenReturn(new VoterInformation());
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.empty());

		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);
	}

	@Test
	public void testBlockVotingCardIgnoreUnableShouldNotBlock() throws Exception {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.CAST);
		votingCardStateMock.setAttempts(0);

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId))
				.thenReturn(new VoterInformation());
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardStateMock));

		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);

		assertEquals(VotingCardStates.CAST, votingCardStateMock.getState());
	}

	@Test
	public void testBlockVotingCardIgnoreUnableNotFound() throws Exception {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.CAST);
		votingCardStateMock.setAttempts(0);

		when(voterInformationRepository.getByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId))
				.thenReturn(new VoterInformation());
		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardStateMock));

		votingCardStateService.blockVotingCardIgnoreUnable(tenantId, electionEventId, votingCardId);
	}

	@Test
	public void testUpdateVotingCardStateSuccessful() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.CAST);

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.of(votingCardStateMock));

		votingCardStateService.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.SENT_BUT_NOT_CAST);

		assertEquals(VotingCardStates.SENT_BUT_NOT_CAST, votingCardStateMock.getState());
	}

	@Test
	public void testInitializeVotingCardStateSuccessful() throws DuplicateEntryException, EntryPersistenceException, ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setTenantId(tenantId);
		votingCardStateMock.setElectionEventId(electionEventId);
		votingCardStateMock.setVotingCardId(votingCardId);
		votingCardStateMock.setState(VotingCardStates.NONE);

		when(votingCardRepositoryMock.acquire(tenantId, electionEventId, votingCardId)).thenReturn(Optional.empty());

		votingCardStateService.initializeVotingCardState(votingCardStateMock);

		assertEquals(VotingCardStates.NOT_SENT, votingCardStateMock.getState());
	}

	private Optional<VotingCardState> notSent(String tenantId, String electionEventId, String votingCardId) {
		VotingCardState state = new VotingCardState();
		state.setTenantId(tenantId);
		state.setElectionEventId(electionEventId);
		state.setVotingCardId(votingCardId);
		state.setState(VotingCardStates.NOT_SENT);
		return Optional.of(state);
	}

	private Optional<VotingCardState> sentNotCast(String tenantId, String electionEventId, String votingCardId) {
		VotingCardState state = new VotingCardState();
		state.setTenantId(tenantId);
		state.setElectionEventId(electionEventId);
		state.setVotingCardId(votingCardId);
		state.setState(VotingCardStates.SENT_BUT_NOT_CAST);
		return Optional.of(state);
	}

	private Optional<VotingCardState> cast(final String tenantId, final String electionEventId, final String votingCardId) {
		VotingCardState state = new VotingCardState();
		state.setTenantId(tenantId);
		state.setElectionEventId(electionEventId);
		state.setVotingCardId(votingCardId);
		state.setState(VotingCardStates.CAST);
		return Optional.of(state);
	}
}
