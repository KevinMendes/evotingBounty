/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.Json;
import javax.json.JsonReader;

import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

public abstract class VotingCardSetServiceTestBase {

	protected static final String BALLOT_ID = "dd5bd34dcf6e4de4b771a92fa38abc11";
	protected static final String BALLOT_BOX_ID = "4b88e5ec5fc14e14934c2ef491fe2de7";
	protected static final String VOTING_CARD_SET_ID = "74a4e530b24f4086b099d153321cf1b3";

	protected static Path getPathOfFileInResources(final Path path) throws URISyntaxException {
		return Paths.get(VotingCardSetServiceTestBase.class.getClassLoader().getResource(path.toString()).toURI());
	}

	protected String getVotingCardSetWithStatus(final String status) {
		return "{\n" + "\"id\":\"" + VOTING_CARD_SET_ID + "\",\n" + "\"defaultTitle\":\"Default Voting Card Set Title\",\n"
				+ "\"defaultDescription\":\"Default Voting Card Set Description\",\n" + "\"verificationCardSetId\":\"9a0\",\n"
				+ "\"alias\":\"Default VCS Alias\",\n" + "\"numberOfVotingCardsToGenerate\":10,\n" + "\"electionEvent\":{\n"
				+ "\"id\":\"8324e5f17dc9475d891af3051a22e759\"\n" + "},\n" + "\"ballotBox\":{\n" + "\"id\":\"" + BALLOT_BOX_ID + "\"\n" + "},\n"
				+ "\t \"status\": \"" + status + "\"\n" + "}";
	}

	/**
	 * For the voting card set that is returned by the mock repository, specify its status by setting up the repository with a mock of a voting card
	 * set's JSON.
	 *
	 * @param status the status
	 */
	protected void setStatusForVotingCardSetFromRepository(final String status, final VotingCardSetRepository votingCardSetRepositoryMock)
			throws ResourceNotFoundException {
		try (final JsonReader jsonReader = Json.createReader(new StringReader(getVotingCardSetWithStatus(status)))) {
			when(votingCardSetRepositoryMock.getVotingCardSetJson(any(), any())).thenReturn(jsonReader.readObject());
		}
	}

	protected String getBallotBoxWithStatus(final Status status) {
		return "{ \"id\": \"" + BALLOT_BOX_ID
				+ "\", \"defaultTitle\": \"Ballot Box Title\", \"defaultDescription\": \"Ballot Box Description\", \"alias\": \"Ballot Box Alias\", "
				+ "\"dateFrom\": \"12/12/2012\", \"dateTo\": \"14/12/2012\"," + "\"electionEvent\": { \"id\": \"314bd34dcf6e4de4b771a92fa3849d3d\"},"
				+ "\"ballot\": { \"id\": \"" + BALLOT_ID + "\"}, \"electoralAuthority\": { \"id\": \"hhhbd34dcf6e4de4b771a92fa38abhhh\"},"
				+ "\"status\": \"" + status.name() + "\"}";
	}
}
