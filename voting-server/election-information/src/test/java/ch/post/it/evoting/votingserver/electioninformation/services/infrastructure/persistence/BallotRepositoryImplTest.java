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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;

/**
 * Junit tests for the class {@link BallotRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BallotRepositoryImplTest extends BaseRepositoryImplTest<Ballot, Integer> {

	@InjectMocks
	private static final BallotRepository ballotRepository = new BallotRepositoryImpl();
	@Mock
	private TypedQuery<Ballot> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public BallotRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(Ballot.class, ballotRepository.getClass());
	}

	@Before
	public void setup() {
		when(entityManagerMock.createQuery(anyString(), eq(Ballot.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
	}

	@Test
	public void findByTenantIdElectionEventIdBallotId() throws ResourceNotFoundException {
		when(queryMock.getSingleResult()).thenReturn(new Ballot());

		final String tenantId = "2";
		final String ballotId = "2";
		final String electionEventId = "2";
		assertNotNull(ballotRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId));
	}

	@Test
	public void findByTenantIdElectionEventIdBallotIdNotFound() throws ResourceNotFoundException {
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		final String tenantId = "2";
		final String ballotId = "2";
		final String electionEventId = "2";
		ballotRepository.findByTenantIdElectionEventIdBallotId(tenantId, electionEventId, ballotId);
	}
}
