/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.domain.tally;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;

@JsonPropertyOrder({ "electionEventId", "ballotBoxId", "nodeId", "shufflePayloads" })
public class MixDecryptOnlineRequestPayload {

	@JsonProperty
	private final String electionEventId;
	@JsonProperty
	private final String ballotBoxId;
	@JsonProperty
	private final int nodeId;
	@JsonProperty
	private final List<MixnetShufflePayload> shufflePayloads;

	@JsonCreator
	public MixDecryptOnlineRequestPayload(
			@JsonProperty(value = "electionEventId", required = true)
			final String electionEventId,
			@JsonProperty(value = "ballotBoxId", required = true)
			final String ballotBoxId,
			@JsonProperty(value = "nodeId", required = true)
			final int nodeId,
			@JsonProperty(value = "shufflePayloads", required = true)
			final List<MixnetShufflePayload> shufflePayloads) {
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.nodeId = nodeId;
		this.shufflePayloads = checkNotNull(shufflePayloads);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public int getNodeId() {
		return nodeId;
	}

	public List<MixnetShufflePayload> getShufflePayloads() {
		return ImmutableList.copyOf(shufflePayloads);
	}
}
