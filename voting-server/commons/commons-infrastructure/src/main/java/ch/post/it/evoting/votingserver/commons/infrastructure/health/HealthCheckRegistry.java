/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for Health checks. There should be one registry per service. Each Health check should have a distinct name. This class it not thread-safe
 * when registering or running checks
 */
public class HealthCheckRegistry {

	private final Map<HealthCheckValidationType, HealthCheck> registry = new EnumMap<>(HealthCheckValidationType.class);

	/**
	 * Register a new health-check with the specified name.
	 *
	 * @param healthCheckValidationType the name of the health-check
	 * @param healthCheck               the health-check
	 * @return true if registered, false if it already exists an health-check with the same name
	 */
	public boolean register(final HealthCheckValidationType healthCheckValidationType, final HealthCheck healthCheck) {
		registry.putIfAbsent(healthCheckValidationType, healthCheck);
		if (!registry.containsKey(healthCheckValidationType)) {
			registry.put(healthCheckValidationType, healthCheck);
			return true;
		}
		return false;
	}

	/**
	 * unregister an health-check
	 *
	 * @param healthCheckValidationType the name of the health-check
	 */
	public void unregister(final HealthCheckValidationType healthCheckValidationType) {
		registry.remove(healthCheckValidationType);
	}

	/**
	 * run all registered health-checks and return the aggregated result
	 *
	 * @return
	 */
	public HealthCheckStatus runAllChecks() {

		final Map<HealthCheckValidationType, HealthCheck.HealthCheckResult> results = new EnumMap<>(HealthCheckValidationType.class);

		registry.forEach((key, value) -> results.put(key, value.execute()));
		return new HealthCheckStatus(results);
	}

	/**
	 * Receives a set of keys with the validations to skip key with
	 *
	 * @param validationsToSkip
	 * @return
	 */
	public HealthCheckStatus runChecksDifferentFrom(final List<HealthCheckValidationType> validationsToSkip) {

		final Map<HealthCheckValidationType, HealthCheck.HealthCheckResult> results = new EnumMap<>(HealthCheckValidationType.class);

		registry.entrySet().stream().filter(entry -> !validationsToSkip.contains(entry.getKey()))
				.forEach(entry -> results.put(entry.getKey(), entry.getValue().execute()));

		return new HealthCheckStatus(results);

	}
}
