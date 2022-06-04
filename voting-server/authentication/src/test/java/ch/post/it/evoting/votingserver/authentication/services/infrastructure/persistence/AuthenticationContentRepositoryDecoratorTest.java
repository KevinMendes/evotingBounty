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

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationContentRepositoryDecoratorTest {

	public static final String TENANT_ID = "100";
	public static final String ELECTION_EVENT_ID = "100";
	public static final String TRACK_ID = "trackId";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	AuthenticationContent authenticationContentMock;
	@Mock
	private AuthenticationContentRepository authenticationContentRepositoryMock;
	@InjectMocks
	private final AuthenticationContentRepositoryDecorator sut = new AuthenticationContentRepositoryDecorator() {

		@Override
		public AuthenticationContent findByTenantIdElectionEventId(final String tenantId, final String electionEventId) throws ResourceNotFoundException {
			return authenticationContentRepositoryMock.findByTenantIdElectionEventId(tenantId, electionEventId);
		}

		@Override
		public AuthenticationContent find(final Integer integer) {
			return authenticationContentRepositoryMock.find(integer);
		}

		@Override
		public AuthenticationContent update(final AuthenticationContent entity) throws EntryPersistenceException {
			return authenticationContentRepositoryMock.update(entity);
		}

		@Override
		public AuthenticationContent save(final AuthenticationContent entity) throws DuplicateEntryException {
			return super.save(entity);
		}
	};
	@Mock
	private Logger logger;

	@Before
	public void init() {

		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void save() throws ResourceNotFoundException, DuplicateEntryException {

		when(authenticationContentRepositoryMock.save(any(AuthenticationContent.class))).thenReturn(authenticationContentMock);
		final AuthenticationContent save = sut.save(authenticationContentMock);
		assertNotNull(save);

	}

	@Test
	public void saveAndThrowException() throws ResourceNotFoundException, DuplicateEntryException {

		expectedException.expect(DuplicateEntryException.class);
		when(authenticationContentRepositoryMock.save(any(AuthenticationContent.class))).thenThrow(new DuplicateEntryException("exception"));
		sut.save(authenticationContentMock);
	}
}
