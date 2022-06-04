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

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImplTest;

/**
 * Test class for AuthenticationCertsRepository
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationCertsRepositoryImplTest extends BaseRepositoryImplTest<AuthenticationCerts, Integer> {

	@InjectMocks
	private static final AuthenticationCertsRepositoryImpl authenticationCertsRepository = new AuthenticationCertsRepositoryImpl();
	@Mock
	private TypedQuery<AuthenticationCerts> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public AuthenticationCertsRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(AuthenticationCerts.class, authenticationCertsRepository.getClass());
	}

	@Test
	public void findByTenantIdElectionEventId() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(AuthenticationCerts.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(new AuthenticationCerts());

		String tenantId = "2";
		String electionEventId = "2";
		assertNotNull(authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId));
	}

	@Test
	public void findByTenantIdElectionEventIdNotFound() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(AuthenticationCerts.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		String tenantId = "2";
		String electionEventId = "2";
		authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
	}

}
