/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.platform.PlatformCertificate;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)

public class PlatformCertificateRepositoryTest {

	@InjectMocks
	private final PlatformCertificateRepositoryImpl sut = new PlatformCertificateRepositoryImpl();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private EntityManager managerMock;
	@Mock
	private TypedQuery<PlatformCertificate> typedQueryMock;
	@Mock
	private PlatformCertificate platformCaEntityMock;

	@Before
	public void init() {

		MockitoAnnotations.initMocks(this.getClass());
		Mockito.when(managerMock.createQuery(Mockito.anyString(), Mockito.eq(PlatformCertificate.class))).thenReturn(typedQueryMock);

	}

	@Test
	public void getPlatformCA() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		Mockito.when(typedQueryMock.getResultList()).thenThrow(new NoResultException("exception"));
		sut.getRootCACertificate();

	}
}
