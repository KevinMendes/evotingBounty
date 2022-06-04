/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;

@RunWith(MockitoJUnitRunner.class)
public class VoteCastCodeRepositoryDecoratorTest {

	private static final String VOTING_CARD_ID = "11";

	private static final String AUTHENTICATION_TOKEN_SIGNATURE = "signature";

	private final String TENANT_ID = "100";

	private final String ELECTION_EVENT_ID = "1";

	private final String VERIFICATION_CARD_ID = "1";

	@InjectMocks
	VoteCastCodeRepositoryDecorator rut = new VoteCastCodeRepositoryDecorator();

	@Mock
	private VoteCastCodeRepository voteCastCodeRepository;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(VoteCastCodeRepositoryDecoratorTest.class);
	}

	@Test
	public void testGenerateCastCodeSuccessful() throws CryptographicOperationException {
		ConfirmationMessage confirmationMessage = new ConfirmationMessage();
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		when(voteCastCodeRepository
				.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE,
						confirmationMessage)).thenReturn(voteCastMessageMock);

		ShortVoteCastReturnCodeAndComputeResults voteCastMessage = rut
				.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE,
						confirmationMessage);

		assertEquals(voteCastMessageMock, voteCastMessage);
	}

	@Test(expected = CryptographicOperationException.class)
	public void testGenerateCastCodeCryptographicOperationException() throws CryptographicOperationException {
		ConfirmationMessage confirmationMessage = new ConfirmationMessage();

		when(voteCastCodeRepository
				.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE,
						confirmationMessage)).thenThrow(new CryptographicOperationException("exception"));

		rut.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE, confirmationMessage);
	}

	@Test
	public void testGetCastCodeSuccessful() throws ResourceNotFoundException {
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		when(voteCastCodeRepository.getCastCode(TENANT_ID, ELECTION_EVENT_ID, ELECTION_EVENT_ID)).thenReturn(voteCastMessageMock);

		ShortVoteCastReturnCodeAndComputeResults voteCastMessage = rut.getCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID);

		assertEquals(voteCastMessageMock, voteCastMessage);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetCastCodeResourceNotFoundException() throws ResourceNotFoundException {
		when(voteCastCodeRepository.getCastCode(TENANT_ID, ELECTION_EVENT_ID, ELECTION_EVENT_ID))
				.thenThrow(new ResourceNotFoundException("exception"));
		rut.getCastCode(TENANT_ID, ELECTION_EVENT_ID, ELECTION_EVENT_ID);
	}

	@Test
	public void testStoreCastCodeSuccessful() throws ResourceNotFoundException {
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		when(voteCastCodeRepository.storesCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, voteCastMessageMock)).thenReturn(true);

		boolean result = rut.storesCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, voteCastMessageMock);

		assertEquals(true, result);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testStoreCastCodeResourceNotFoundException() throws ResourceNotFoundException {
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		when(voteCastCodeRepository.storesCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, voteCastMessageMock))
				.thenThrow(new ResourceNotFoundException("exception"));

		rut.storesCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, voteCastMessageMock);
	}

	@Test
	public void testVoteCastCodeExistsSuccessful() throws VoteCastCodeRepositoryException {
		when(voteCastCodeRepository.voteCastCodeExists(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID)).thenReturn(true);
		boolean result = rut.voteCastCodeExists(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID);
		assertEquals(true, result);
	}

}
