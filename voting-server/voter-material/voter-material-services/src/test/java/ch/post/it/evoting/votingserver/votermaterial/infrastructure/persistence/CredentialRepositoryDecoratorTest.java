/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.Credential;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialRepository;

@RunWith(MockitoJUnitRunner.class)
public class CredentialRepositoryDecoratorTest {

	private final String TENANT_ID = "1";

	private final String ELECTION_EVENT_ID = "2";

	private final String CREDENTIAL_ID = "4";

	@InjectMocks
	CredentialRepositoryDecorator sut = new CredentialRepositoryDecorator() {

		@Override
		public Credential update(Credential entity) throws EntryPersistenceException {
			return null;
		}

		@Override
		public Credential find(Integer id) {
			return null;
		}
	};

	@Mock
	private CredentialRepository credentialRepository;

	@Test
	public void testFindByTenantIdElectionEventIdCredentialIdSuccessful() throws ResourceNotFoundException {
		Credential credentialMock = new Credential();
		credentialMock.setCredentialId("credentialId");

		Mockito.when(credentialRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID))
				.thenReturn(credentialMock);
		Credential credential = sut.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);

		assertEquals(credentialMock, credential);
	}

	@Test
	public void testHasWithTenantIdElectionEventIdCredentialIdSuccessful() throws ResourceNotFoundException {
		Mockito.when(credentialRepository.hasWithTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID)).thenReturn(true);
		Boolean result = sut.hasWithTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);

		assertTrue(result);
	}

	@Test
	public void testSaveSuccessful() throws DuplicateEntryException {
		Credential credentialMock = new Credential();
		credentialMock.setCredentialId("credentialId");

		sut.save(credentialMock);
	}

	@Test(expected = DuplicateEntryException.class)
	public void testSaveDuplicateEntryException() throws DuplicateEntryException {
		Credential credentialMock = new Credential();
		credentialMock.setCredentialId("credentialId");

		Mockito.when(credentialRepository.save(credentialMock)).thenThrow(DuplicateEntryException.class);
		sut.save(credentialMock);
	}

}
