/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

@RunWith(MockitoJUnitRunner.class)
public class VotingCardStateServiceDecoratorTest {

	@InjectMocks
	private final VotingCardStateServiceDecorator sut = new VotingCardStateServiceDecorator() {

		// Need to implement empty unused functions
		@Override
		public void writeIdAndStateOfInactiveVotingCards(String tenantId, String electionEventId, OutputStream stream) throws IOException {
		}

		@Override
		public void incrementVotingCardAttempts(String tenantId, String electionEventId, String votingCardId)
				throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		}

		@Override
		public void initializeVotingCardState(VotingCardState votingCardState)
				throws DuplicateEntryException, EntryPersistenceException, ResourceNotFoundException {
		}

		@Override
		public void blockVotingCardIgnoreUnable(String tenantId, String electionEventId, String votingCardId)
				throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		}
	};
	@Mock
	VotingCardStateService votingCardStateService;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(VotingCardStateServiceDecoratorTest.class);
	}

	@Test
	public void testGetVotingCardStateSuccessful() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.SENT_BUT_NOT_CAST);

		when(votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId)).thenReturn(votingCardStateMock);
		VotingCardState votingCardStateResult = sut.getVotingCardState(tenantId, electionEventId, votingCardId);

		assertEquals(votingCardStateMock, votingCardStateResult);
	}

	@Test(expected = ApplicationException.class)
	public void testGetVotingCardStateApplicationException() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		VotingCardState votingCardStateMock = new VotingCardState();
		votingCardStateMock.setState(VotingCardStates.SENT_BUT_NOT_CAST);

		when(votingCardStateService.getVotingCardState(tenantId, electionEventId, votingCardId)).thenThrow(new ApplicationException("exception"));
		sut.getVotingCardState(tenantId, electionEventId, votingCardId);
	}

	@Test
	public void updateVotingCardStateSuccessful() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		sut.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.NOT_SENT);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void updateVotingCardStateResourceNotFoundException() throws ResourceNotFoundException, ApplicationException, DuplicateEntryException {
		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";

		doThrow(ResourceNotFoundException.class).when(votingCardStateService)
				.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.NOT_SENT);
		sut.updateVotingCardState(tenantId, electionEventId, votingCardId, VotingCardStates.NOT_SENT);
	}

}
