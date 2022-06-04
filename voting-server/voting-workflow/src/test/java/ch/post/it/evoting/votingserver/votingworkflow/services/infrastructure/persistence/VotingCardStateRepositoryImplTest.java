/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStatePK;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateRepository;

/**
 * The Class VotingCardStateRepositoryImplTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class VotingCardStateRepositoryImplTest extends BaseRepositoryImplTest<VotingCardState, Long> {

	/**
	 * The voting card state repository.
	 */
	@InjectMocks
	private static final VotingCardStateRepository votingCardStateRepository = new VotingCardStateRepositoryImpl();

	/**
	 * Creates a new object of the testing class.
	 *
	 * @throws InstantiationException the instantiation exception
	 * @throws IllegalAccessException the illegal access exception
	 */
	public VotingCardStateRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(VotingCardState.class, votingCardStateRepository.getClass());
	}

	/**
	 * Test acquire by tenant id election event id voting card id.
	 *
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Test
	public void testFindByTenantIdElectionEventIdVotingCardId() throws ResourceNotFoundException {

		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		VotingCardStatePK pk = new VotingCardStatePK(tenantId, electionEventId, votingCardId);
		when(entityManagerMock.find(VotingCardState.class, pk, LockModeType.PESSIMISTIC_WRITE)).thenReturn(new VotingCardState());

		assertTrue(votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId).isPresent());
	}

	/**
	 * Test acquire by tenant id election event id voting card id not found.
	 *
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Test
	public void testFindByTenantIdElectionEventIdVotingCardIdNotFound() throws ResourceNotFoundException {

		String tenantId = "2";
		String electionEventId = "2";
		String votingCardId = "2";
		VotingCardStatePK pk = new VotingCardStatePK(tenantId, electionEventId, votingCardId);
		when(entityManagerMock.find(VotingCardState.class, pk, LockModeType.PESSIMISTIC_WRITE)).thenThrow(new PersistenceException());

		assertFalse(votingCardStateRepository.acquire(tenantId, electionEventId, votingCardId).isPresent());
	}

}
