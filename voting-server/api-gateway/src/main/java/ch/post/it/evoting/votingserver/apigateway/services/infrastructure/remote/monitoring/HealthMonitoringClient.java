/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.monitoring;

import ch.post.it.evoting.votingserver.commons.infrastructure.health.HealthCheckStatus;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * The Interface of health client for monitoring.
 */
public interface HealthMonitoringClient {

	@GET("check")
	Call<HealthCheckStatus> checkHealth();
}
