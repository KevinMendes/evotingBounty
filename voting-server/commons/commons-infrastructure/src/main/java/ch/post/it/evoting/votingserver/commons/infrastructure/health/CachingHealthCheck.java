/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import java.time.Instant;

/**
 * This class caches the result of a health check for a certain amount of time. It is not thread-safe! It caches the last result during a certain
 * time-period after which it will call the real method and update the "timer"
 */
public class CachingHealthCheck extends HealthCheck {

	private final HealthCheck delegate;
	private final int timeToLive;
	private Instant lastExecution;
	private HealthCheckResult lastResult = null;

	public CachingHealthCheck(HealthCheck delegate, int timeToLiveInSeconds) {
		this.delegate = delegate;
		this.timeToLive = timeToLiveInSeconds;
		this.lastExecution = Instant.MIN;
	}

	@Override
	protected HealthCheckResult check() {
		// first time?
		if (lastResult == null) {
			lastResult = delegate.execute();
			lastExecution = Instant.now();
			return lastResult;
		}

		// it will always execute the real call after the defined period has passed since the last real
		// execution
		Instant now = Instant.now();
		// not enough time has passed since last execution, return cached result
		if (lastExecution.plusSeconds(timeToLive).isAfter(now)) {
			return lastResult;
		} else {
			lastResult = delegate.execute();
			lastExecution = Instant.now();
			return lastResult;
		}
	}
}
