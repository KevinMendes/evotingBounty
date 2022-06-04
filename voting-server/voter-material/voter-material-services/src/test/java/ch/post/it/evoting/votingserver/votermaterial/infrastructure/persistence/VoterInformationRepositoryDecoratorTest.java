/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;

@RunWith(MockitoJUnitRunner.class)
public class VoterInformationRepositoryDecoratorTest {

	private final String TENANT_ID = "1";

	private final String ELECTION_EVENT_ID = "2";

	private final String VOTING_CARD_ID = "3";

	private final String CREDENTIAL_ID = "4";

	@InjectMocks
	VoterInformationRepositoryDecorator sut = new VoterInformationRepositoryDecorator() {

		@Override
		public VoterInformation update(VoterInformation entity) throws EntryPersistenceException {
			return null;
		}

		@Override
		public VoterInformation find(Integer id) {
			return null;
		}
	};

	@Mock
	private VoterInformationRepository voterInformationRepository;

	@Test
	public void testCountByTenantIdElectionEventIdAndSearchTermsSuccessful() throws ResourceNotFoundException {

		long mockResult = 10l;

		Mockito.when(voterInformationRepository.countByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, ""))
				.thenReturn(mockResult);

		long actualResult = sut.countByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, "");

		assertEquals(mockResult, actualResult);
	}

	@Test
	public void testFindByTenantIdElectionEventIdAndSearchTermsSuccessful() throws ResourceNotFoundException {

		List<VoterInformation> voterInformationListMock = new ArrayList<VoterInformation>();
		voterInformationListMock.add(new VoterInformation());

		Mockito.when(voterInformationRepository.findByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, "", 0, 100))
				.thenReturn(voterInformationListMock);

		List<VoterInformation> voterInformationList = sut.findByTenantIdElectionEventIdAndSearchTerms(TENANT_ID, ELECTION_EVENT_ID, "", 0, 100);

		assertEquals(voterInformationListMock, voterInformationList);
	}

	@Test
	public void testFindByTenantIdElectionEventIdCredentialIdSuccessful() throws ResourceNotFoundException {
		VoterInformation voterInformationMock = Mockito.mock(VoterInformation.class);

		Mockito.when(voterInformationRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID))
				.thenReturn(voterInformationMock);
		VoterInformation voterInformation = sut.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);

		assertEquals(voterInformation, voterInformationMock);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindByTenantIdElectionEventIdCredentialIdNotFound() throws ResourceNotFoundException {
		Mockito.when(voterInformationRepository.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID))
				.thenThrow(ResourceNotFoundException.class);
		sut.findByTenantIdElectionEventIdCredentialId(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
	}

	@Test
	public void testFindByTenantIdElectionEventIdVotingCardIdSuccessful() throws ResourceNotFoundException {
		VoterInformation voterInformationMock = Mockito.mock(VoterInformation.class);

		Mockito.when(voterInformationRepository.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(voterInformationMock);
		VoterInformation voterInformation = sut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		assertEquals(voterInformation, voterInformationMock);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testFindByTenantIdElectionEventIdVotingCardIdNotFound() throws ResourceNotFoundException {
		Mockito.when(voterInformationRepository.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenThrow(ResourceNotFoundException.class);
		sut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}

	@Test
	public void testHasWithTenantIdElectionEventIdVotingCardIdSuccessful() {
		Mockito.when(voterInformationRepository.hasWithTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(true);
		boolean result = sut.hasWithTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		assertTrue(result);
	}

	@Test
	public void testSaveSuccessful() throws DuplicateEntryException {
		sut.save(Mockito.mock(VoterInformation.class));
	}

	@Test(expected = DuplicateEntryException.class)
	public void testSaveDuplicateException() throws DuplicateEntryException {
		Mockito.when(voterInformationRepository.save(Mockito.any())).thenThrow(DuplicateEntryException.class);
		sut.save(Mockito.mock(VoterInformation.class));
	}
}
