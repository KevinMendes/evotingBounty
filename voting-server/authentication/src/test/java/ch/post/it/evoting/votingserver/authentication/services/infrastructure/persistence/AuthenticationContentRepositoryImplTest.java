/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

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

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;

/**
 * Test class for AuthenticationContentRepository
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationContentRepositoryImplTest extends BaseRepositoryImplTest<AuthenticationContent, Integer> {

	@InjectMocks
	private static final AuthenticationContentRepositoryImpl authenticationContentRepository = new AuthenticationContentRepositoryImpl();
	@Mock
	private TypedQuery<AuthenticationContent> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public AuthenticationContentRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(AuthenticationContent.class, authenticationContentRepository.getClass());
	}

	@Test
	public void findByTenantIdElectionEventId() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(AuthenticationContent.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(new AuthenticationContent());

		String tenantId = "2";
		String electionEventId = "2";
		assertNotNull(authenticationContentRepository.findByTenantIdElectionEventId(tenantId, electionEventId));
	}

	@Test
	public void findByTenantIdElectionEventIdNotFound() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(AuthenticationContent.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		String tenantId = "2";
		String electionEventId = "2";
		authenticationContentRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
	}

}
