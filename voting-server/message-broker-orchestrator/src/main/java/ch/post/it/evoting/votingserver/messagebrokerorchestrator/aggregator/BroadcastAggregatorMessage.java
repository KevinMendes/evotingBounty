/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastAggregatorMessage {

	private final String correlationId;

	@JsonCreator
	public BroadcastAggregatorMessage(
			@JsonProperty("correlationId")
			final String correlationId) {
		this.correlationId = correlationId;
	}

	@JsonProperty
	public String getCorrelationId() {
		return correlationId;
	}
}