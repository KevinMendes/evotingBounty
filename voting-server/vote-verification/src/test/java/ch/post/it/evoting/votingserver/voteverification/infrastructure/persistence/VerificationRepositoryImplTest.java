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
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;

/**
 * Junits for the class {@link VerificationRepository}
 */
@RunWith(MockitoJUnitRunner.class)
public class VerificationRepositoryImplTest extends BaseRepositoryImplTest<Verification, Integer> {

	@InjectMocks
	private static final VerificationRepository verificationRepository = new VerificationRepositoryImpl();

	@Mock
	private TypedQuery<Verification> queryMock;

	/**
	 * Creates a new object of the testing class.
	 */
	public VerificationRepositoryImplTest() throws InstantiationException, IllegalAccessException {
		super(Verification.class, verificationRepository.getClass());
	}

	@Test
	public void testFindByTenantIdElectionEventIdVerificationCardId() throws ResourceNotFoundException {
		String tenantId = "1";
		String electionEventId = "1";
		String verificationCardId = "1";
		when(entityManagerMock.createQuery(anyString(), eq(Verification.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenReturn(new Verification());

		assertNotNull(verificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId));
	}

	@Test
	public void testFindByTenantIdElectionEventIdVerificationCardIdNotFound() throws ResourceNotFoundException {
		String tenantId = "2";
		String electionEventId = "2";
		String verificationCardId = "2";
		when(entityManagerMock.createQuery(anyString(), eq(Verification.class))).thenReturn(queryMock);
		when(queryMock.setParameter(anyString(), anyString())).thenReturn(queryMock);
		when(queryMock.getSingleResult()).thenThrow(new NoResultException());

		expectedException.expect(ResourceNotFoundException.class);

		assertNotNull(verificationRepository.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId));
	}
}
