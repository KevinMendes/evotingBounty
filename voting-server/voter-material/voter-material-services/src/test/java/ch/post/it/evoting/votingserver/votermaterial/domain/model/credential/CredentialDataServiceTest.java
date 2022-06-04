/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.credential;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class CredentialDataServiceTest {

	private final String TENANT_ID = "1";
	private final String ELECTION_EVENT_ID = "2";
	private final String VOTING_CARD_ID = "3";

	@InjectMocks
	CredentialDataService sut = new CredentialDataService();

	// Must be mocked, even if not use in this test class
	@Mock
	private Logger logger;

	@Mock
	private CredentialRepository credentialRepository;

	@Test
	public void testGetCredentialDataSuccessful() throws ResourceNotFoundException {
		Credential credentialMock = new Credential();
		credentialMock.setCredentialId("credentialId");

		Mockito.when(credentialRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(credentialMock);
		Credential credential = sut.getCredentialData(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		assertEquals(credentialMock.getCredentialId(), credential.getCredentialId());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetCredentialDataResourceNotFound() throws ResourceNotFoundException {
		Mockito.when(credentialRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenThrow(ResourceNotFoundException.class);
		sut.getCredentialData(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}

}
