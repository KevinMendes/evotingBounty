/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.services.domain.model.platform.EaPlatformCaEntity;

@RunWith(MockitoJUnitRunner.class)
public class PlatformCARepositoryImplTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@InjectMocks
	EaPlatformCARepositoryImpl classUnderTest = new EaPlatformCARepositoryImpl();
	@Mock
	private EntityManager manager;
	@Mock
	private TypedQuery<EaPlatformCaEntity> typedQuery;

	@Before
	public void init() {

		MockitoAnnotations.initMocks(this.getClass());

	}

	@Test
	public void findPlatformCA() throws ResourceNotFoundException {

		final List<EaPlatformCaEntity> list = Arrays.asList(new EaPlatformCaEntity());
		when(manager.createQuery(anyString(), eq(EaPlatformCaEntity.class))).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(list);
		Assert.assertNotNull(classUnderTest.getRootCACertificate());
	}

	@Test
	public void findPlatformCAResourceNotFoundException() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(manager.createQuery(anyString(), eq(EaPlatformCaEntity.class))).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenThrow(new NoResultException("exception"));
		Assert.assertNotNull(classUnderTest.getRootCACertificate());
	}

}
