/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * The Class VoterInformationServiceTest.
 */
public class VoterInformationServiceTest {

	@InjectMocks
	private final VoterInformationService classUnderTest = new VoterInformationService();

	@Mock
	private VoterInformationRepository mockVoterInformationRepository;

	// Must be mocked, even if not use in this test class
	@Mock
	private Logger logger;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test search voting cards happy path.
	 *
	 * @throws ResourceNotFoundException    the resource not found exception
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testSearchVotingCardsHappyPath() throws ResourceNotFoundException, UnsupportedEncodingException {

		/* Preparation */
		String tenantId = "100";
		String electionEventId = "1";
		String votingCardId = "vc";
		int offset = 0;
		int limit = 10;

		VoterInformation voterInformation = new VoterInformation();
		List<VoterInformation> mockedList = new ArrayList<VoterInformation>();
		mockedList.add(voterInformation);

		/* Expectations */
		Mockito.when(
				mockVoterInformationRepository.findByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, votingCardId, offset, limit))
				.thenReturn(mockedList);

		/* Execution */
		List<VoterInformation> results = classUnderTest.searchVoterInformation(tenantId, electionEventId, votingCardId, offset, limit);

		/* verification */
		assertNotNull(results);
		assertEquals(1, results.size());

	}

	/**
	 * Test search voting cardsra not found.
	 *
	 * @throws ResourceNotFoundException    the resource not found exception
	 * @throws UnsupportedEncodingException
	 */
	@Test(expected = ResourceNotFoundException.class)
	public void testSearchVotingCardsraNotFound() throws ResourceNotFoundException, UnsupportedEncodingException {

		/* Preparation */
		String tenantId = "100";
		String electionEventId = "1";
		String votingCardId = "vc";
		int offset = 0;
		int limit = 10;

		VoterInformation voterInformation = new VoterInformation();
		List<VoterInformation> mockedList = new ArrayList<VoterInformation>();
		mockedList.add(voterInformation);

		/* Expectations */
		Mockito.when(
				mockVoterInformationRepository.findByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, votingCardId, offset, limit))
				.thenThrow(new ResourceNotFoundException("Test"));

		/* Execution */
		classUnderTest.searchVoterInformation(tenantId, electionEventId, votingCardId, offset, limit);

	}

	@Test
	public void testCountByTenantIdElectionEventIdAndSearchTermsSuccessful() throws ResourceNotFoundException, UnsupportedEncodingException {
		/* Preparation */
		String tenantId = "100";
		String electionEventId = "1";
		String idSearchTerm = "searchTerm";
		long countResultMock = 1;

		VoterInformation voterInformation = new VoterInformation();
		List<VoterInformation> mockedList = new ArrayList<VoterInformation>();
		mockedList.add(voterInformation);

		/* Expectations */
		Mockito.when(mockVoterInformationRepository.countByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, idSearchTerm))
				.thenReturn(countResultMock);

		/* Execution */
		long result = classUnderTest.getCountOfVotingCardsForSearchTerms(tenantId, electionEventId, idSearchTerm);

		assertEquals(countResultMock, result);
	}

}
