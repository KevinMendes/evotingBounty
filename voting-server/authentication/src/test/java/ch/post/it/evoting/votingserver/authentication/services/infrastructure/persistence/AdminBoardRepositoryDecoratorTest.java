/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AdminBoardRepositoryDecoratorTest {

	public static final String ADMIN_BOARD_ID = "100";
	public static final String TENANT_ID = "100";
	public static final String ELECTION_EVENT_ID = "100";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private Logger logger;
	@Mock
	private AdminBoardRepository adminBoardRepository;
	@InjectMocks
	private final AdminBoardRepositoryDecorator sut = new AdminBoardRepositoryDecorator() {
		@Override
		public AdminBoard findByTenantIdElectionEventId(String tenantId, String electionEventId) throws ResourceNotFoundException {
			return adminBoardRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		}

		@Override
		public AdminBoard find(Integer integer) {
			return adminBoardRepository.find(integer);
		}

		@Override
		public AdminBoard update(AdminBoard entity) throws EntryPersistenceException {
			return adminBoardRepository.update(entity);
		}

		@Override
		public AdminBoard save(final AdminBoard entity) throws DuplicateEntryException {
			return super.save(entity);
		}
	};
	@Mock
	private AdminBoard adminBoardMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void save() throws ResourceNotFoundException, DuplicateEntryException {

		when(adminBoardRepository.save(any(AdminBoard.class))).thenReturn(adminBoardMock);
		final AdminBoard save = sut.save(adminBoardMock);
		assertNotNull(save);

	}

	@Test
	public void saveAndThrowException() throws ResourceNotFoundException, DuplicateEntryException {

		expectedException.expect(DuplicateEntryException.class);
		when(adminBoardRepository.save(any(AdminBoard.class))).thenThrow(new DuplicateEntryException("exception"));
		sut.save(adminBoardMock);
	}

}
