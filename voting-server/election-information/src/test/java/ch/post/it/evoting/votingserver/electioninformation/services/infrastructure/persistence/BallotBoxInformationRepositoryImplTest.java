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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;

/**
 * Test class for the ballot box info repository
 */
@RunWith(MockitoJUnitRunner.class)
public class BallotBoxInformationRepositoryImplTest extends BaseRepositoryImplTest<BallotBoxInformation, Integer> {

	@InjectMocks
	private static final BallotBoxInformationRepositoryImpl ballotBoxInformationRepository = new BallotBoxInformationRepositoryImpl();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private TypedQuery<BallotBoxInformation> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public BallotBoxInformationRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(BallotBoxInformation.class, ballotBoxInformationRepository.getClass());
	}

	@Test
	public void findByExternalIdAndTenantId() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBoxInformation.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(new BallotBoxInformation());

		final String tenantId = "2";
		final String ballotBoxId = "2";
		final String electionEventId = "2";
		assertNotNull(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void findNonExistentBallotBoxInfo() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBoxInformation.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(NoResultException.class);

		expectedException.expect(ResourceNotFoundException.class);

		final String tenantId = "2";
		final String ballotBoxId = "2";
		final String electionEventId = "2";
		ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId);
	}
}
