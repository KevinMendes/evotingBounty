/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.Credential;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialRepository;

/**
 * Junits for the class {@link CredentialRepositoryImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class CredentialRepositoryImplTest extends BaseRepositoryImplTest<Credential, Integer> {

	@InjectMocks
	private static final CredentialRepository credentialRepository = new CredentialRepositoryImpl();

	@Mock
	private TypedQuery<Credential> queryMock;

	@Mock
	private TypedQuery<Long> queryMockLong;

	/**
	 * Creates a new object of the testing class.
	 */
	public CredentialRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(Credential.class, credentialRepository.getClass());
	}

	@Test
	public void testFindByTenantIdElectionEventIdVotingCardId() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "2";
		String credentialId = "3";

		Credential credentialMock = new Credential();
		credentialMock.setCredentialId("credentialId");
		credentialMock.setData("data");
		credentialMock.setElectionEventId(electionEventId);
		credentialMock.setId(3);
		credentialMock.setTenantId(tenantId);

		when(entityManagerMock.createQuery(anyString(), eq(Credential.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(credentialMock);

		Credential credentialResult = credentialRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

		assertEquals(electionEventId, credentialResult.getElectionEventId());
		assertEquals(tenantId, credentialResult.getTenantId());
	}

	@Test
	public void testFindByTenantIdElectionEventIdVotingCardIdNotFound() throws ResourceNotFoundException {
		String tenantId = "2";
		String electionEventId = "2";
		String credentialId = "2";
		when(entityManagerMock.createQuery(anyString(), eq(Credential.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		assertNotNull(credentialRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId));
	}

	@Test
	public void testHasWithTenantIdElectionEventIdCredentialIdSuccessful() {
		String tenantId = "2";
		String electionEventId = "2";
		String credentialId = "2";

		when(entityManagerMock.createQuery(anyString(), eq(Long.class))).thenReturn(queryMockLong);
		when(queryMockLong.setParameter(anyString(), anyString())).thenReturn(queryMockLong);
		when(queryMockLong.getSingleResult()).thenReturn(10l);

		boolean actualResult = credentialRepository.hasWithTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

		assertTrue(actualResult);
	}
}
