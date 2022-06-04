/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class groups all results into a global 'status'.
 */
public class HealthCheckStatus {

	private Map<HealthCheckValidationType, HealthCheck.HealthCheckResult> results;

	private HealthCheckStatus() {
		/* needed for jackson */
	}

	public HealthCheckStatus(final Map<HealthCheckValidationType, HealthCheck.HealthCheckResult> results) {
		Objects.requireNonNull(results);
		this.results = results;
	}

	@JsonIgnore
	public boolean isHealthy() {
		// status is healthy if ALL checks are 'ok'
		return results.values().stream().allMatch(HealthCheck.HealthCheckResult::getHealthy);
	}

	public Map<HealthCheckValidationType, HealthCheck.HealthCheckResult> getResults() {
		return results;
	}
}
