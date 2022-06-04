/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.returncodes.ComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.commons.util.ZipUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.AdditionalData;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;

public class BallotBoxFactory {

	private static final String VOTE = "vote";

	private static final String AUTHENTICATION_TOKEN = "authenticationToken";

	private static final String ADDITIONAL_DATA = "additionalData";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	public BallotBox from(Vote vote, ComputeResults computationResults, String authenticationToken, List<AdditionalData> additionalData) {
		BallotBox ballotBox = informationFrom(vote);
		final String voteAsJSON = obtainVote(vote, authenticationToken, additionalData);
		ballotBox.setVote(voteAsJSON);

		try {
			ballotBox.setComputationResults(ZipUtils.zipText(objectMapper.writeValueAsString(computationResults)));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}

		return ballotBox;
	}

	private BallotBox informationFrom(Vote vote) {
		BallotBox ballotBox = new BallotBox();
		ballotBox.setTenantId(vote.getTenantId());
		ballotBox.setElectionEventId(vote.getElectionEventId());
		ballotBox.setVotingCardId(vote.getVotingCardId());
		ballotBox.setBallotId(vote.getBallotId());
		ballotBox.setBallotBoxId(vote.getBallotBoxId());
		return ballotBox;
	}

	private String obtainVote(Vote vote, String authenticationToken, List<AdditionalData> additionalData) {
		JsonObjectBuilder jsonVoteBuilder = Json.createObjectBuilder();
		try {
			JsonObject voteJson = JsonUtils.getJsonObject(objectMapper.writeValueAsString(vote));
			JsonObject authenticationTokenJson = JsonUtils.getJsonObject(authenticationToken);
			jsonVoteBuilder.add(VOTE, voteJson).add(AUTHENTICATION_TOKEN, authenticationTokenJson);
			if (null != additionalData && !additionalData.isEmpty()) {
				JsonArray additionalDataJson = JsonUtils.getJsonArray(objectMapper.writeValueAsString(additionalData));
				jsonVoteBuilder.add(ADDITIONAL_DATA, additionalDataJson);
			}
			return jsonVoteBuilder.build().toString();
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public VoteAndComputeResults to(BallotBox ballotBox) throws IOException {
		VoteAndComputeResults voteAndComputeResults = new VoteAndComputeResults();
		ComputeResults computeResults = objectMapper.readValue(ZipUtils.unzip(ballotBox.getComputationResults()), ComputeResults.class);
		voteAndComputeResults.setComputeResults(computeResults);

		final String voteAsJSON = ballotBox.getVote();
		Vote vote = restoreVote(voteAsJSON);
		voteAndComputeResults.setVote(vote);

		return voteAndComputeResults;
	}

	private Vote restoreVote(String jsonWithVote) throws IOException {

		JsonObject jsonObjectWithVote = JsonUtils.getJsonObject(jsonWithVote);
		JsonObject voteJson = jsonObjectWithVote.getJsonObject(VOTE);
		return objectMapper.readValue(voteJson.toString(), Vote.class);
	}
}
