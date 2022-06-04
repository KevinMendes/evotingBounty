/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import org.springframework.context.ApplicationEvent;

public class LocalAggregatorApplicationEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private final String correlationId;

	public LocalAggregatorApplicationEvent(Object source, String correlationId) {
		super(source);
		this.correlationId = correlationId;
	}

	public String getCorrelationId() {
		return correlationId;
	}
}
