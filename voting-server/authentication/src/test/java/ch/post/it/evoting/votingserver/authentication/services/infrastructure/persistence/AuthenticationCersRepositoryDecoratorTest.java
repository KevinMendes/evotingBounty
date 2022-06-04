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

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationCersRepositoryDecoratorTest {

	public static final String TENANT_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private Logger logger;
	@Mock
	private AuthenticationCerts authenticationCertsMock;

	@Mock
	private AuthenticationCertsRepository authenticationCertsRepository;
	@InjectMocks
	private final AuthenticationCertsRepositoryDecorator sut = new AuthenticationCertsRepositoryDecorator() {
		@Override
		public AuthenticationCerts findByTenantIdElectionEventId(String tenantId, String electionEventId) throws ResourceNotFoundException {
			return authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		}

		@Override
		public AuthenticationCerts find(Integer integer) {
			return authenticationCertsRepository.find(integer);
		}

		@Override
		public AuthenticationCerts update(AuthenticationCerts entity) throws EntryPersistenceException {
			return authenticationCertsRepository.update(entity);
		}

		@Override
		public AuthenticationCerts save(final AuthenticationCerts entity) throws DuplicateEntryException {
			return super.save(entity);
		}
	};

	@Before
	public void init() {

		MockitoAnnotations.initMocks(AuthenticationCersRepositoryDecoratorTest.class);
	}

	@Test
	public void save() throws ResourceNotFoundException, DuplicateEntryException {

		when(authenticationCertsRepository.save(any(AuthenticationCerts.class))).thenReturn(authenticationCertsMock);
		final AuthenticationCerts save = sut.save(authenticationCertsMock);
		assertNotNull(save);

	}

	@Test
	public void saveAndThrowException() throws ResourceNotFoundException, DuplicateEntryException {

		expectedException.expect(DuplicateEntryException.class);
		when(authenticationCertsRepository.save(any(AuthenticationCerts.class))).thenThrow(new DuplicateEntryException("exception"));
		sut.save(authenticationCertsMock);
	}

}
