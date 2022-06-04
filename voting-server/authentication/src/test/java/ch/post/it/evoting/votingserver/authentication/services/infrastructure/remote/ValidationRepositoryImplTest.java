/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;

@RunWith(MockitoJUnitRunner.class)
public class ValidationRepositoryImplTest {

	private static final String TENANT_ID = "100";

	private static final String BALLOT_BOX_ID = "100";

	private static final String ELECTION_EVENT_ID = "100";
	private static final String TRACK_ID = "trackId";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private Logger logger;
	@Mock
	private TrackIdInstance trackIdInstance;
	@Mock
	private ElectionInformationClient electionInformationClient;
	@InjectMocks
	private final ValidationRepositoryImpl validationRepository = new ValidationRepositoryImpl(electionInformationClient);

	@Before
	public void init() {
		when(trackIdInstance.getTrackId()).thenReturn(TRACK_ID);
	}

	@Test
	public void validate() throws ResourceNotFoundException, IOException {

		@SuppressWarnings("unchecked")
		final Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.success(new ValidationResult(true)));

		when(electionInformationClient.validateElectionInDates(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		final ValidationResult result = validationRepository.validateElectionInDates(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);

		assertTrue(result.isResult());
	}

	@Test
	public void validateThrowsResultNotFoundException() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		doThrow(ResourceNotFoundException.class).when(electionInformationClient)
				.validateElectionInDates(anyString(), anyString(), anyString(), anyString(), anyString());

		validationRepository.validateElectionInDates(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);
	}

	@Test
	public void validateThrowsRetrofitError() throws ResourceNotFoundException, IOException {
		@SuppressWarnings("unchecked")
		final Call<ValidationResult> callMock = (Call<ValidationResult>) Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(retrofit2.Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		expectedException.expect(RetrofitException.class);
		when(electionInformationClient.validateElectionInDates(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(callMock);
		validationRepository.validateElectionInDates(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);

		validationRepository.validateElectionInDates(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);
	}
}
