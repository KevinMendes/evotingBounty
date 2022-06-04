/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@RunWith(MockitoJUnitRunner.class)
public class VoteRepositoryImplTest {

	private final String TENANT_ID = "100";

	private final String ELECTION_EVENT_ID = "1";

	private final String VOTING_CARD_ID = "1";

	private final String TRACK_ID = "1";

	private final VoteAndComputeResults VOTE = new VoteAndComputeResults();

	// has to correspond with Application.properties:PATH_VOTES
	private final String PATH_VOTES_VALUE = "votes";

	@Mock
	private TrackIdInstance trackId;

	@Mock
	private ElectionInformationClient electionInformationClient;
	@InjectMocks
	VoteRepositoryImpl rut = new VoteRepositoryImpl(electionInformationClient);

	@Mock
	private Logger LOGGER;

	@Before
	public void init() {
		when(trackId.getTrackId()).thenReturn(TRACK_ID);
	}

	@Test
	public void testfindVotingCardIdSuccessful() throws ResourceNotFoundException, IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<VoteAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(VOTE));

		when(electionInformationClient.getVote(TRACK_ID, PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(callMock);

		VoteAndComputeResults vote = rut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		Assert.assertEquals(VOTE, vote);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testfindVotingCardIdError404() throws ResourceNotFoundException, IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<VoteAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.getVote(TRACK_ID, PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(callMock);

		rut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}

	@Test(expected = Exception.class)
	public void testfindVotingCardIdError500() throws ResourceNotFoundException, IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<VoteAndComputeResults> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.getVote(TRACK_ID, PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID)).thenReturn(callMock);

		rut.findByTenantIdElectionEventIdVotingCardId(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}

	@Test
	public void testSaveSuccessful() throws IOException, ResourceNotFoundException {
		VoteAndComputeResults voteMock = new VoteAndComputeResults();
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);

		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));
		String authenticationTokenJsonString = "";

		when(electionInformationClient
				.saveVote(trackId.getTrackId(), PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, voteMock, authenticationTokenJsonString))
				.thenReturn(callMock);
		rut.save(TENANT_ID, ELECTION_EVENT_ID, voteMock, authenticationTokenJsonString);
	}

	@Test
	public void testVoteExistsSuccessful() throws IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.success(ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVote(trackId.getTrackId(), PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		boolean result = rut.voteExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		Assert.assertTrue(result);
	}

	@Test
	public void testVoteExists404NotFound() throws IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVote(trackId.getTrackId(), PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		boolean result = rut.voteExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

		Assert.assertFalse(result);
	}

	@Test(expected = Exception.class)
	public void testVoteExists500Error() throws IOException, VoteRepositoryException {
		@SuppressWarnings("unchecked")
		Call<ResponseBody> callMock = Mockito.mock(Call.class);
		when(callMock.execute()).thenReturn(Response.error(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0])));

		when(electionInformationClient.checkVote(trackId.getTrackId(), PATH_VOTES_VALUE, TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID))
				.thenReturn(callMock);

		rut.voteExists(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
	}
}
