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

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;

@JsonPropertyOrder({ "electionEventId", "ballotBoxId", "initialPayload", "shufflePayloads" })
public class MixDecryptOnlinePayload {

	@JsonProperty
	private final String electionEventId;
	@JsonProperty
	private final String ballotBoxId;
	@JsonProperty
	private final MixnetInitialPayload initialPayload;
	@JsonProperty
	private final List<MixnetShufflePayload> shufflePayloads;

	@JsonCreator
	public MixDecryptOnlinePayload(
			@JsonProperty(value = "electionEventId", required = true)
			final String electionEventId,
			@JsonProperty(value = "ballotBoxId", required = true)
			final String ballotBoxId,
			@JsonProperty(value = "initialPayload", required = true)
			final MixnetInitialPayload initialPayload,
			@JsonProperty(value = "shufflePayloads", required = true)
			final List<MixnetShufflePayload> shufflePayloads) {
		this.electionEventId = validateUUID(electionEventId);
		this.ballotBoxId = validateUUID(ballotBoxId);
		this.initialPayload = initialPayload;
		this.shufflePayloads = checkNotNull(shufflePayloads);
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getBallotBoxId() {
		return ballotBoxId;
	}

	public MixnetInitialPayload getInitialPayload() {
		return initialPayload;
	}

	public List<MixnetShufflePayload> getShufflePayloads() {
		return shufflePayloads;
	}
}
