/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Test class of the admin board repository
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminBoardRepositoryTest {

	public static final String TENANT_ID = "100";
	public static final String ELECTION_EVENT_ID = "100";
	public static final String VOTING_CARD_ID = "100";
	@InjectMocks
	private final AdminBoardRepository repository = new AdminBoardRepositoryImpl();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private EntityManager entityManagerMock;
	@Mock
	private TypedQuery<AdminBoard> typedQueryMock;

	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void findByTenantIdElectionEventId() throws ResourceNotFoundException {

		final List<AdminBoard> result = Arrays.asList(new AdminBoard());
		when(entityManagerMock.createQuery(anyString(), eq(AdminBoard.class))).thenReturn(typedQueryMock);
		when(typedQueryMock.getSingleResult()).thenReturn(result.get(0));
		Assert.assertNotNull(repository.findByTenantIdElectionEventId(TENANT_ID, ELECTION_EVENT_ID));

	}

	@Test
	public void findByTenantIdElectionEventIdNotFound() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(entityManagerMock.createQuery(anyString(), eq(AdminBoard.class))).thenReturn(typedQueryMock);
		when(typedQueryMock.getSingleResult()).thenThrow(new NoResultException("exception"));
		Assert.assertNotNull(repository.findByTenantIdElectionEventId(TENANT_ID, ELECTION_EVENT_ID));

	}

}
