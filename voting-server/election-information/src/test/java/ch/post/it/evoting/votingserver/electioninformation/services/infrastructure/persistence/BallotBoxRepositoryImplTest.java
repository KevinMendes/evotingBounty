/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.opencsv.CSVWriter;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.logging.service.VoteHashService;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.AdditionalData;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard.VotingCardWriter;

/**
 * The class <code>VoteRepositoryImplTest</code> contains tests for the class {@link <code>VoteRepositoryImpl</code>}
 */
@RunWith(MockitoJUnitRunner.class)
public class BallotBoxRepositoryImplTest {

	public static final String VOTING_CARD_ID = "1";
	private static final String encryptedOptions = "132412342134;asdfasdfasdf.AVeryLongStringWithTheEncryptedOptionsafasdf12312423asdfnmlourjvlmaldjfoqwer14ad34fas1avjsjjj;.";
	@Spy
	@InjectMocks
	private static BallotBoxRepository ballotBoxRepository = new BallotBoxRepositoryImpl();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	protected EntityManager entityManagerMock;
	@Mock
	private TypedQuery<BallotBox> queryBBMock;
	@Mock
	private TypedQuery<String> queryStringMock;
	@Mock
	private VoteValidationRepository voteValidationRepository;
	@Spy
	private BallotBoxFactory ballotBoxFactory = new BallotBoxFactory();
	@Mock
	private VoteHashService voteHashService;
	@Mock
	private Logger LOGGER;

	@Test
	public void saveBBNull() throws DuplicateEntryException {
		final Vote vote = null;
		final String authenticationToken = null;
		final ComputeResults computeResults = null;

		expectedException.expect(NullPointerException.class);

		ballotBoxRepository.save(ballotBoxFactory.from(vote, computeResults, authenticationToken, null));
	}

	@Test
	public void saveBBFailedPersistenceException() throws DuplicateEntryException {
		final String ballotId = "";
		final String ballotBoxId = "";
		final String tenantId = "";

		final Vote vote = new Vote();
		vote.setTenantId(tenantId);
		vote.setBallotId(ballotId);
		vote.setBallotBoxId(ballotBoxId);
		vote.setEncryptedOptions(encryptedOptions);

		final ComputeResults computationResults = new ComputeResults();
		computationResults.setComputationResults("computationResults");
		computationResults.setDecryptionResults("decryptionResults");

		final String authenticationToken = "{\"id\":\"fF8vg9sZna6OXhVtDHL1Vw==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"100\",\"votingCardId\":\"b421dc7d1bb50fd06c3327d126113818\",\"ballotId\":\"100\",\"credentialId\":\"100\",\"verificationCardId\":\"100\",\"ballotBoxId\":\"100\",\"votingCardSetId\":\"100\",\"verificationCardSetId\":\"100\"},\"timestamp\":\"${__time()}\",\"signature\":\"US669kapfNwGrc3LeoZoTbTT80zarHNnsR0KelfPiC0MlvFZjMVWnEcdH+DEcf6HSXYVbn6MhwmGacOYwu2c/zDPTc1GUE3lDiqwkfc0EQp1+SMwNrKxEJWqk8sUe1cApnUmwWzBQsypxycqsayu8yVg/RtVqzHv/JrIoDG7d3Cdw43NisvJBgHmDNNpmnj7ZJJzJRz+eh8REC8x3bHSxLARwjqFtW1+2ugQJfs1eGwN4blBkHTK5rbulZ+h8R5dw/nXXxsq80gZSX359mo3kxaa6OF6Pt4sVwju5BHceQse6gIkSs8Ol1d9nbyRxOsyXbEYvo0NW9ei7TrIzmwYbA==\"}";

		final String exceptionMessage = "Persistence Exception Message";
		doThrow(new PersistenceException(exceptionMessage)).when(ballotBoxRepository).save(any(BallotBox.class));

		expectedException.expect(PersistenceException.class);
		expectedException.expectMessage(exceptionMessage);

		ballotBoxRepository.save(ballotBoxFactory.from(vote, computationResults, authenticationToken, null));
	}

	@Test
	public void saveBB() throws DuplicateEntryException {
		final String ballotId = "ballotId";
		final String ballotBoxId = "ballotBoxId";
		final String tenantId = "tenantId";

		final Vote vote = new Vote();
		vote.setTenantId(tenantId);
		vote.setBallotId(ballotId);
		vote.setBallotBoxId(ballotBoxId);
		vote.setEncryptedOptions(encryptedOptions);

		final ComputeResults computationResults = new ComputeResults();
		computationResults.setComputationResults("computationResults");
		computationResults.setDecryptionResults("decryptionResults");

		final String authenticationToken = "{\"id\":\"fF8vg9sZna6OXhVtDHL1Vw==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"100\",\"votingCardId\":\"b421dc7d1bb50fd06c3327d126113818\",\"ballotId\":\"100\",\"credentialId\":\"100\",\"verificationCardId\":\"100\",\"ballotBoxId\":\"100\",\"votingCardSetId\":\"100\",\"verificationCardSetId\":\"100\"},\"timestamp\":\"${__time()}\",\"signature\":\"US669kapfNwGrc3LeoZoTbTT80zarHNnsR0KelfPiC0MlvFZjMVWnEcdH+DEcf6HSXYVbn6MhwmGacOYwu2c/zDPTc1GUE3lDiqwkfc0EQp1+SMwNrKxEJWqk8sUe1cApnUmwWzBQsypxycqsayu8yVg/RtVqzHv/JrIoDG7d3Cdw43NisvJBgHmDNNpmnj7ZJJzJRz+eh8REC8x3bHSxLARwjqFtW1+2ugQJfs1eGwN4blBkHTK5rbulZ+h8R5dw/nXXxsq80gZSX359mo3kxaa6OF6Pt4sVwju5BHceQse6gIkSs8Ol1d9nbyRxOsyXbEYvo0NW9ei7TrIzmwYbA==\"}";

		doReturn(new BallotBox()).when(ballotBoxRepository).save(any(BallotBox.class));

		assertNotNull(ballotBoxRepository.save(ballotBoxFactory.from(vote, computationResults, authenticationToken, null)));
	}

	@Test
	public void saveBBAdditionalData() throws DuplicateEntryException {
		final String ballotId = "ballotId";
		final String ballotBoxId = "ballotBoxId";
		final String tenantId = "tenantId";

		final Vote vote = new Vote();
		vote.setTenantId(tenantId);
		vote.setBallotId(ballotId);
		vote.setBallotBoxId(ballotBoxId);
		vote.setEncryptedOptions(encryptedOptions);

		final ComputeResults computationResults = new ComputeResults();
		computationResults.setComputationResults("computationResults");
		computationResults.setDecryptionResults("decryptionResults");

		final String authenticationToken = "{\"id\":\"fF8vg9sZna6OXhVtDHL1Vw==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"100\",\"votingCardId\":\"b421dc7d1bb50fd06c3327d126113818\",\"ballotId\":\"100\",\"credentialId\":\"100\",\"verificationCardId\":\"100\",\"ballotBoxId\":\"100\",\"votingCardSetId\":\"100\",\"verificationCardSetId\":\"100\"},\"timestamp\":\"${__time()}\",\"signature\":\"US669kapfNwGrc3LeoZoTbTT80zarHNnsR0KelfPiC0MlvFZjMVWnEcdH+DEcf6HSXYVbn6MhwmGacOYwu2c/zDPTc1GUE3lDiqwkfc0EQp1+SMwNrKxEJWqk8sUe1cApnUmwWzBQsypxycqsayu8yVg/RtVqzHv/JrIoDG7d3Cdw43NisvJBgHmDNNpmnj7ZJJzJRz+eh8REC8x3bHSxLARwjqFtW1+2ugQJfs1eGwN4blBkHTK5rbulZ+h8R5dw/nXXxsq80gZSX359mo3kxaa6OF6Pt4sVwju5BHceQse6gIkSs8Ol1d9nbyRxOsyXbEYvo0NW9ei7TrIzmwYbA==\"}";

		final List<AdditionalData> additionalData = new ArrayList<>();
		final String key = "key1";
		final String value = "value1";
		additionalData.add(new AdditionalData(key, value));

		doReturn(new BallotBox()).when(ballotBoxRepository).save(any(BallotBox.class));

		assertNotNull(ballotBoxRepository.save(ballotBoxFactory.from(vote, computationResults, authenticationToken, additionalData)));
	}

	@Test
	public void saveBBEmptyAdditionalData() throws DuplicateEntryException {
		final String ballotId = "ballotId";
		final String ballotBoxId = "ballotBoxId";
		final String tenantId = "tenantId";

		final Vote vote = new Vote();
		vote.setTenantId(tenantId);
		vote.setBallotId(ballotId);
		vote.setBallotBoxId(ballotBoxId);
		vote.setEncryptedOptions(encryptedOptions);

		final ComputeResults computationResults = new ComputeResults();
		computationResults.setComputationResults("computationResults");
		computationResults.setDecryptionResults("decryptionResults");

		final String authenticationToken = "{\"id\":\"fF8vg9sZna6OXhVtDHL1Vw==\",\"voterInformation\":{\"tenantId\":\"100\",\"electionEventId\":\"100\",\"votingCardId\":\"b421dc7d1bb50fd06c3327d126113818\",\"ballotId\":\"100\",\"credentialId\":\"100\",\"verificationCardId\":\"100\",\"ballotBoxId\":\"100\",\"votingCardSetId\":\"100\",\"verificationCardSetId\":\"100\"},\"timestamp\":\"${__time()}\",\"signature\":\"US669kapfNwGrc3LeoZoTbTT80zarHNnsR0KelfPiC0MlvFZjMVWnEcdH+DEcf6HSXYVbn6MhwmGacOYwu2c/zDPTc1GUE3lDiqwkfc0EQp1+SMwNrKxEJWqk8sUe1cApnUmwWzBQsypxycqsayu8yVg/RtVqzHv/JrIoDG7d3Cdw43NisvJBgHmDNNpmnj7ZJJzJRz+eh8REC8x3bHSxLARwjqFtW1+2ugQJfs1eGwN4blBkHTK5rbulZ+h8R5dw/nXXxsq80gZSX359mo3kxaa6OF6Pt4sVwju5BHceQse6gIkSs8Ol1d9nbyRxOsyXbEYvo0NW9ei7TrIzmwYbA==\"}";

		final List<AdditionalData> additionalData = new ArrayList<>();

		doReturn(new BallotBox()).when(ballotBoxRepository).save(any(BallotBox.class));

		assertNotNull(ballotBoxRepository.save(ballotBoxFactory.from(vote, computationResults, authenticationToken, additionalData)));
	}

	@Test
	public void findByTenantIdElectionEventIdVotingCardId() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBox.class))).thenReturn(queryBBMock);
		when(queryBBMock.setParameter(anyString(), anyString())).thenReturn(queryBBMock);
		final ArrayList<BallotBox> result = new ArrayList<>();
		result.add(new BallotBox());
		when(queryBBMock.getResultList()).thenReturn(result);

		final String tenantId = "2";
		final String votingCardId = "2";
		final String electionEventId = "2";
		assertNotNull(ballotBoxRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId));
	}

	@Test
	public void findByTenantIdElectionEventIdVotingCardIdNotFound() throws ResourceNotFoundException {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBox.class))).thenReturn(queryBBMock);
		when(queryBBMock.setParameter(anyString(), anyString())).thenReturn(queryBBMock);
		final ArrayList<BallotBox> result = new ArrayList<>();
		when(queryBBMock.getResultList()).thenReturn(result);

		expectedException.expect(ResourceNotFoundException.class);

		final String tenantId = "2";
		final String votingCardId = "2";
		final String electionEventId = "2";
		ballotBoxRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
	}

	@Test
	public void findByTenantIdElectionEventIdNotEmpty() {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBox.class))).thenReturn(queryBBMock);
		when(queryBBMock.setParameter(anyString(), anyString())).thenReturn(queryBBMock);
		final ArrayList<BallotBox> result = new ArrayList<>();
		result.add(new BallotBox());
		when(queryBBMock.getResultList()).thenReturn(result);

		final String tenantId = "2";
		final String electionEventId = "2";
		final String ballotBoxId = "3";
		assertNotNull(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId));
		assertFalse(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId).isEmpty());
	}

	@Test
	public void findByTenantIdElectionEventIdEmpty() {
		when(entityManagerMock.createQuery(anyString(), eq(BallotBox.class))).thenReturn(queryBBMock);
		when(queryBBMock.setParameter(anyString(), anyString())).thenReturn(queryBBMock);
		when(queryBBMock.getResultList()).thenReturn(new ArrayList<>());

		final String tenantId = "2";
		final String electionEventId = "2";
		final String ballotBoxId = "3";
		assertNotNull(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId));
		assertTrue(ballotBoxRepository.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId).isEmpty());
	}

	@Test
	public void writeUsedVotingCardsReturnsEmptyIfNoVotesFound() throws Exception {

		// given
		final Session mockSession = mock(Session.class);
		when(entityManagerMock.unwrap(eq(Session.class))).thenReturn(mockSession);
		final NativeQuery mockQuery = mock(NativeQuery.class);
		when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
		final ScrollableResults mockResults = mock(ScrollableResults.class);
		when(mockResults.next()).thenReturn(false);
		when(mockQuery.scroll(any(ScrollMode.class))).thenReturn(mockResults);
		when(mockSession.createQuery(anyString())).thenReturn(mockQuery);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final VotingCardWriter writer = spy(new VotingCardWriter(baos));
		final String tenantId = "100";
		final String electionEventId = "100";
		// when
		ballotBoxRepository.findAndWriteUsedVotingCards(tenantId, electionEventId, writer);
		writer.close();

		// then (check that we didn't write anything)
		verify(writer, times(0)).write(any());
		final byte[] bytes = baos.toByteArray();
		// i'm assuming that we write text data (csv)
		Assert.assertEquals("", new String(bytes, StandardCharsets.UTF_8));

	}

	@Test
	public void writeUsedVotingCardsReturnsSomethingIfVotesFound() throws Exception {

		// given
		final Session mockSession = mock(Session.class);
		when(entityManagerMock.unwrap(eq(Session.class))).thenReturn(mockSession);
		final NativeQuery mockQuery = mock(NativeQuery.class);
		when(mockQuery.setParameter(anyInt(), any())).thenReturn(mockQuery);
		final ScrollableResults mockResults = mock(ScrollableResults.class);
		when(mockResults.next()).thenReturn(true).thenReturn(false);
		when(mockResults.get(eq(0))).thenReturn(VOTING_CARD_ID);
		when(mockQuery.scroll(any(ScrollMode.class))).thenReturn(mockResults);
		when(mockSession.createQuery(anyString())).thenReturn(mockQuery);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final VotingCardWriter writer = spy(new VotingCardWriter(baos));
		final String tenantId = "100";
		final String electionEventId = "100";

		// when
		ballotBoxRepository.findAndWriteUsedVotingCards(tenantId, electionEventId, writer);
		writer.close();
		final byte[] bytes = baos.toByteArray();
		// i'm assuming that we write text data (csv)
		final String actualVotingCardId = new String(bytes, StandardCharsets.UTF_8);

		// then (csv writer always writes value + newline)
		verify(writer, times(1)).write(any());
		Assert.assertEquals(VOTING_CARD_ID + CSVWriter.DEFAULT_LINE_END, actualVotingCardId);

	}
}
