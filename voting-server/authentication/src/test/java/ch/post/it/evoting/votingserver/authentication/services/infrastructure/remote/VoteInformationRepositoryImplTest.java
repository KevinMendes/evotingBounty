/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import retrofit2.Call;

/**
 * Test class for
 */
@RunWith(MockitoJUnitRunner.class)
public class VoteInformationRepositoryImplTest {

	private static final String TENANT_ID = "100";

	private static final String CREDENTIAL_ID = "100";

	private static final String ELECTION_EVENT_ID = "100";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private TrackIdInstance trackIdMock;
	@Mock
	private VoterMaterialClient voterMaterialClient;
	@InjectMocks
	private final VoterInformationRepositoryImpl voterInformationRepository = new VoterInformationRepositoryImpl(voterMaterialClient);
	@Mock
	private Logger logger;
	@Mock
	private VoterInformation voterInformationMock;

	@Test
	public void findByTenantIdElectionEventIdCredentialId() throws ResourceNotFoundException, IOException {

		@SuppressWarnings("unchecked")
		final Call<VoterInformation> callMock = (Call<VoterInformation>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(voterInformationMock));

		when(voterMaterialClient.getVoterInformation(any(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		final VoterInformation byTenantIdElectionEventIdCredentialId = voterInformationRepository
				.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
		assertNotNull(byTenantIdElectionEventIdCredentialId);
	}

	@Test
	public void findByTenantIdElectionEventIdCredentialIdThrowResourceNotFoundException() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		doThrow(ResourceNotFoundException.class).when(voterMaterialClient)
				.getVoterInformation(any(), anyString(), anyString(), anyString(), anyString());

		voterInformationRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
	}

}
