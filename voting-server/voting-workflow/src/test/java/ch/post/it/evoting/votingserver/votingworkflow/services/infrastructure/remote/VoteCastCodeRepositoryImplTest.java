/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.TraceableConfirmationMessage;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class VoteCastCodeRepositoryImplTest {

	private static final String AUTHENTICATION_TOKEN_SIGNATURE = "signature";

	private final String TENANT_ID = "100";

	private final String ELECTION_EVENT_ID = "1";

	private final String VOTING_CARD_ID = "3cc7e2a0dd394fae8d9bd2ebd4fa4b95";

	private final String VERIFICATION_CARD_ID = "4dffac3879e443d3a3634929f6a2eb07";

	private final String TRACK_ID = "2";

	private final String PATH_CAST_CODE_VALUE = "castcodes";

	@Mock
	private TrackIdInstance trackId;

	@Mock
	private VerificationClient verificationClient;

	@Mock
	private ElectionInformationClient electionInformationClient;
	@InjectMocks
	VoteCastCodeRepositoryImpl rut = new VoteCastCodeRepositoryImpl(verificationClient, electionInformationClient);
	@Mock
	private Logger LOGGER;

	@Before
	public void init() {
		when(trackId.getTrackId()).thenReturn(TRACK_ID);
	}

	@Test
	public void testGenerateCastCodeSuccessful() throws CryptographicOperationException, IOException {
		TraceableConfirmationMessage confirmationMessage = new TraceableConfirmationMessage();
		confirmationMessage.setConfirmationKey("1");

		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		@SuppressWarnings("unchecked")
		Call<ShortVoteCastReturnCodeAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(voteCastMessageMock));

		when(verificationClient
				.generateCastCode(eq(TRACK_ID), eq(PATH_CAST_CODE_VALUE), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(VERIFICATION_CARD_ID), any()))
				.thenReturn(callMock);

		ShortVoteCastReturnCodeAndComputeResults voteCastMessage = rut
				.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE,
						confirmationMessage);

		assertEquals(voteCastMessageMock, voteCastMessage);
	}

	@Test(expected = CryptographicOperationException.class)
	public void testGenerateCastCodeRetrofitError() throws CryptographicOperationException, IOException {
		TraceableConfirmationMessage confirmationMessage = new TraceableConfirmationMessage();
		confirmationMessage.setConfirmationKey("1");

		@SuppressWarnings("unchecked")
		Call<ShortVoteCastReturnCodeAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(verificationClient
				.generateCastCode(eq(TRACK_ID), eq(PATH_CAST_CODE_VALUE), eq(TENANT_ID), eq(ELECTION_EVENT_ID), eq(VERIFICATION_CARD_ID), any()))
				.thenReturn(callMock);

		rut.generateCastCode(TENANT_ID, ELECTION_EVENT_ID, VERIFICATION_CARD_ID, VOTING_CARD_ID, AUTHENTICATION_TOKEN_SIGNATURE, confirmationMessage);
	}

	@Test
	public void testGetCastcodeSuccessful() throws ResourceNotFoundException, IOException {
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		@SuppressWarnings("unchecked")
		Call<ShortVoteCastReturnCodeAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(voteCastMessageMock));

		when(electionInformationClient.getVoteCastCode(TRACK_ID, PATH_CAST_CODE_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		ShortVoteCastReturnCodeAndComputeResults voteCastMessage = rut.getCastCode(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		assertEquals(voteCastMessageMock, voteCastMessage);
	}

	@Test
	public void testStoresCastCodeSuccessful() throws ResourceNotFoundException, IOException {
		ShortVoteCastReturnCodeAndComputeResults voteCastMessageMock = new ShortVoteCastReturnCodeAndComputeResults();

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(true);

		@SuppressWarnings("unchecked")
		Call<ValidationResult> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(validationResultMock));

		when(electionInformationClient
				.storeCastCode(TRACK_ID, PATH_CAST_CODE_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, voteCastMessageMock))
				.thenReturn(callMock);

		boolean result = rut.storesCastCode(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, voteCastMessageMock);

		assertEquals(validationResultMock.isResult(), result);
	}

	@Test
	public void testVoteCastCodeExistsSuccessful() throws IOException, VoteCastCodeRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVoteCastCode(TRACK_ID, PATH_CAST_CODE_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		boolean result = rut.voteCastCodeExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
		assertEquals(true, result);
	}

	@Test
	public void testVoteCastCodeExists404NotFound() throws IOException, VoteCastCodeRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVoteCastCode(TRACK_ID, PATH_CAST_CODE_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		boolean result = rut.voteCastCodeExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		assertEquals(false, result);
	}

	@Test(expected = Exception.class)
	public void testVoteCastCodeExists500Error() throws IOException, VoteCastCodeRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVoteCastCode(TRACK_ID, PATH_CAST_CODE_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		rut.voteCastCodeExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}

}
