/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.persistence;

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
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextRepository;

@RunWith(MockitoJUnitRunner.class)
public class ElectionEventContextRepositoryImplTest extends BaseRepositoryImplTest<ElectionEventContextEntity, Integer> {

	@InjectMocks
	private static final ElectionEventContextRepository electionEventContextRepository = new ElectionEventContextRepositoryImpl();
	private final String electionEventId = "e3e3c2fd8a16489291c5c24e7b74b26e";
	@Mock
	private TypedQuery<ElectionEventContextEntity> queryMock;

	public ElectionEventContextRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(ElectionEventContextEntity.class, electionEventContextRepository.getClass());
	}

	@Test
	public void testFindByElectionEventId() throws ResourceNotFoundException {
		final ElectionEventContextEntity electionEventContextEntity = new ElectionEventContextEntity();
		electionEventContextEntity.setElectionEventId(electionEventId);
		when(entityManagerMock.createQuery(anyString(), eq(ElectionEventContextEntity.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(electionEventContextEntity);

		assertNotNull(electionEventContextRepository.findByElectionEventId(electionEventId));
	}

	@Test
	public void testFindByTenantIdElectionEventIdVerificationCardIdNotFound() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(ElectionEventContextEntity.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		assertNotNull(electionEventContextRepository.findByElectionEventId(electionEventId));
	}
}
