/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.common.exception.OException;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseFixture;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Tests of {@link VotingCardSetRepository}.
 */
class VotingCardSetRepositoryTest {

	private static final String VOTING_CARD_SET_ID = "1d9bf23fecd24f899c30b11fe1a6cb5f";
	private static final String BALLOT_BOX_ID = "268872255b9b44f39c5404f3ebd85c07";
	private static final String ELECTION_EVENT_ID = "101549c5a4a04c7b88a0cb9be8ab3df6";
	private static final String INVALID_VOTING_CARD_SET_ID = "1d9";
	private static final int NUMBER_OF_VOTING_CARDS = 3;

	private DatabaseFixture fixture;
	private BallotBoxRepository ballotBoxRepository;
	private BallotRepository ballotRepository;
	private VotingCardSetRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		DatabaseManager manager = fixture.databaseManager();
		ballotBoxRepository = mock(BallotBoxRepository.class);
		ballotRepository = mock(BallotRepository.class);
		repository = new VotingCardSetRepository(manager);
		repository.ballotBoxRepository = ballotBoxRepository;
		repository.ballotRepository = ballotRepository;
		repository.initialize();
		URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(repository.entityName(), resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testGetBallotBoxId() {
		assertEquals(BALLOT_BOX_ID, repository.getBallotBoxId(VOTING_CARD_SET_ID));
	}

	@Test
	void testGetBallotBoxIdNotFound() {
		final String ballotBoxId = "101549c5a4a04c7b88a0cb9be8ab3df6";
		assertTrue(repository.getBallotBoxId(ballotBoxId).isEmpty());
	}

	@Test
	void testUpdateRelatedBallot() {
		JsonObject ballotBox = Json.createObjectBuilder().add(JsonConstants.ALIAS, "ballotBoxAlias")
				.add(JsonConstants.BALLOT, Json.createObjectBuilder().add(JsonConstants.ID, "ballotId")).build();
		when(ballotBoxRepository.find(BALLOT_BOX_ID)).thenReturn(ballotBox.toString());
		JsonObject ballot = Json.createObjectBuilder().add(JsonConstants.ALIAS, "ballotAlias").build();
		when(ballotRepository.find("ballotId")).thenReturn(ballot.toString());
		repository.updateRelatedBallot(singletonList(VOTING_CARD_SET_ID));
		String json = repository.find(VOTING_CARD_SET_ID);
		JsonObject object = JsonUtils.getJsonObject(json);
		assertEquals("ballotBoxAlias", object.getString(JsonConstants.BALLOT_BOX_ALIAS));
		assertEquals("ballotAlias", object.getString(JsonConstants.BALLOT_ALIAS));
	}

	@Test
	void testUpdateRelatedVerificationCardSet() {
		repository.updateRelatedVerificationCardSet(VOTING_CARD_SET_ID, "verificationCardSetId");
		String json = repository.find(VOTING_CARD_SET_ID);
		assertEquals("verificationCardSetId", JsonUtils.getJsonObject(json).getString(JsonConstants.VERIFICATION_CARD_SET_ID));
	}

	@Test
	void testUpdateRelatedVerificationCardSetNotFound() {
		assertThrows(DatabaseException.class, () -> repository.updateRelatedVerificationCardSet("unknownVotingCardSet", "verificationCardSetId"));
	}

	@Test
	void testListByElectionEvent() {
		String json = repository.listByElectionEvent(ELECTION_EVENT_ID);
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(1, array.size());
		JsonObject object = array.getJsonObject(0);
		assertEquals(VOTING_CARD_SET_ID, object.getString(JsonConstants.ID));

	}

	@Test
	void testListByElectionEventUnknown() {
		String json = repository.listByElectionEvent("unknownElectionEvent");
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertTrue(array.isEmpty());
	}

	@Test
	void testGetVotingCardSetAlias() {
		final String alias = repository.getVotingCardSetAlias(VOTING_CARD_SET_ID);
		assertEquals("vcs_133", alias);
	}

	@Test
	void testGetVotingCardSetAliasWithNullAndInvalidParameter() {
		assertThrows(NullPointerException.class, () -> repository.getVotingCardSetAlias(null));
		assertThrows(FailedValidationException.class, () -> repository.getVotingCardSetAlias(INVALID_VOTING_CARD_SET_ID));
	}

	@Test
	void testGetNumberOfVotingCards() throws ResourceNotFoundException {
		int numberOfVotingCards = repository.getNumberOfVotingCards(ELECTION_EVENT_ID, VOTING_CARD_SET_ID);
		assertEquals(NUMBER_OF_VOTING_CARDS, numberOfVotingCards);
	}

	@Test
	void testGetNumberOfVotingCardsWithInvalidParameters() {
		assertThrows(ResourceNotFoundException.class, () -> repository.getNumberOfVotingCards("", VOTING_CARD_SET_ID));
		assertThrows(ResourceNotFoundException.class, () -> repository.getNumberOfVotingCards(ELECTION_EVENT_ID, ""));
	}

	@Test
	void testFindAllVotingCardSetIds() {
		final List<String> votingCardSetIds = repository.findAllVotingCardSetIds(ELECTION_EVENT_ID);
		assertEquals(Collections.singletonList("1d9bf23fecd24f899c30b11fe1a6cb5f"), votingCardSetIds);
	}

	@Test
	void testFindAllVotingCardSetIdsFailedValidation() {
		assertThrows(FailedValidationException.class, () -> repository.findAllVotingCardSetIds("ELECTION_EVENT_ID"));
	}
}
