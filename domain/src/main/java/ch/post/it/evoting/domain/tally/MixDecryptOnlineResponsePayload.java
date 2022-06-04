/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.domain.tally;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;

@JsonPropertyOrder({"initialPayload", "shufflePayload" })
public class MixDecryptOnlineResponsePayload {
	@JsonProperty
	private final MixnetInitialPayload initialPayload;
	@JsonProperty
	private final MixnetShufflePayload shufflePayload;

	@JsonCreator
	public MixDecryptOnlineResponsePayload(
			@JsonProperty(value = "initialPayload")
			final MixnetInitialPayload initialPayload,
			@JsonProperty(value = "shufflePayload", required = true)
			final MixnetShufflePayload shufflePayload) {

		checkNotNull(shufflePayload);
		if (initialPayload != null) {
			checkArgument(initialPayload.getElectionEventId().equals(shufflePayload.getElectionEventId()));
			checkArgument(initialPayload.getBallotBoxId().equals(shufflePayload.getBallotBoxId()));
		}
		this.initialPayload = initialPayload;
		this.shufflePayload = shufflePayload;
	}

	public MixnetInitialPayload getInitialPayload() {
		return initialPayload;
	}

	public MixnetShufflePayload getShufflePayload() {
		return shufflePayload;
	}
}
