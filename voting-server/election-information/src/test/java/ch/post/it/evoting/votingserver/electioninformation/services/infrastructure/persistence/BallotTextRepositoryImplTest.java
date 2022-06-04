/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotText;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotTextRepository;

/**
 * Junit tests for the class {@link BallotTextRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BallotTextRepositoryImplTest extends BaseRepositoryImplTest<BallotText, Integer> {

	@InjectMocks
	private static final BallotTextRepository ballotTextRepository = new BallotTextRepositoryImpl();
	@Mock
	private TypedQuery<BallotText> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public BallotTextRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(BallotText.class, ballotTextRepository.getClass());
	}

	@Test
	public void findByBallotIdElectionEventIdTenantId() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotText.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(new BallotText());

		final String ballotId = "2";
		final String tenantId = "2";
		final String electionEventId = "2";
		assertNotNull(ballotTextRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId));
	}

	@Test
	public void findByBallotIdElectionEventIdTenantIdException() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotText.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		final String ballotId = "1";
		final String tenantId = "1";
		final String electionEventId = "1";
		ballotTextRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
	}
}
