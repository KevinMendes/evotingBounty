/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.tenant.TenantKeystore;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreEntity;

@RunWith(MockitoJUnitRunner.class)
public class AuTenantKeystoreRepositoryImplTest {

	public static final String KEY_TYPE = "ENCRYPT";
	public static final String TENANT_ID = "100";
	@InjectMocks
	private final AuTenantKeystoreRepositoryImpl sut = new AuTenantKeystoreRepositoryImpl();

	@Mock
	private EntityManager managerMock;

	@Mock
	private TenantKeystore tenantKeystoreMock;

	@Mock
	private TypedQuery<TenantKeystore> typedQueryMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void checkIfKeystoreExists() {

		when(managerMock.createQuery(anyString(), eq(TenantKeystore.class))).thenReturn(typedQueryMock);
		final List<TenantKeystore> list = Arrays.asList(new TenantKeystore());
		when(typedQueryMock.getResultList()).thenReturn(list);
		assertTrue(sut.checkIfKeystoreExists(TENANT_ID, KEY_TYPE));

	}

	@Test
	public void checkIfKeystoreExistsReturnFalse() {

		when(managerMock.createQuery(anyString(), eq(TenantKeystore.class))).thenReturn(typedQueryMock);
		when(typedQueryMock.getResultList()).thenReturn(new ArrayList<>());
		assertFalse(sut.checkIfKeystoreExists(TENANT_ID, KEY_TYPE));

	}

	@Test
	public void getTenantKeystore() {

		when(managerMock.createQuery(anyString(), eq(TenantKeystore.class))).thenReturn(typedQueryMock);
		final List<TenantKeystore> list = Arrays.asList(tenantKeystoreMock);
		when(tenantKeystoreMock.getKeyType()).thenReturn(KEY_TYPE);
		when(typedQueryMock.getResultList()).thenReturn(list);
		final TenantKeystoreEntity byTenantAndType = sut.getByTenantAndType(TENANT_ID, KEY_TYPE);
		assertThat(byTenantAndType, notNullValue());
		assertThat(byTenantAndType.getKeyType(), is(KEY_TYPE));

	}
}
