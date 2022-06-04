/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base (abstract) class for health checks. Specific kinds of health checks should inherit and overload the abstract 'check' method.
 */
public abstract class HealthCheck {

	private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheck.class);

	public HealthCheckResult execute() {
		try {
			return check();
		} catch (Exception ex) {
			LOGGER.debug(ex.getMessage(), ex);
			return HealthCheckResult.unhealthy(ex.getMessage());
		}
	}

	protected abstract HealthCheckResult check();

	/**
	 * Class that carries the result of an health check
	 */
	public static class HealthCheckResult {

		private boolean healthy;
		private String message;
		private String timestamp;

		@SuppressWarnings("unused")
		private HealthCheckResult() {/* needed for jacskon */}

		public HealthCheckResult(boolean healthy, final String message, final Instant instant) {
			this.healthy = healthy;
			this.message = message;
			this.timestamp = instant.toString(); // system default clock
		}

		public static HealthCheckResult healthy() {
			return new HealthCheckResult(true, null, Instant.now());
		}

		public static HealthCheckResult healthy(final String message) {
			return new HealthCheckResult(true, message, Instant.now());
		}

		public static HealthCheckResult unhealthy(final Exception ex) {
			return new HealthCheckResult(false, ex.getMessage(), Instant.now());
		}

		public static HealthCheckResult unhealthy(final String message) {
			return new HealthCheckResult(false, message, Instant.now());
		}

		public static HealthCheckResult unhealthy(final String messageFormat, final String... args) {
			return new HealthCheckResult(false, String.format(messageFormat, (Object[]) args), Instant.now());
		}

		public boolean getHealthy() {
			return healthy;
		}

		public void setHealthy(boolean healthy) {
			this.healthy = healthy;
		}

		public String getMessage() {
			return message == null ? "" : message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String instant) {
			timestamp = instant;
		}
	}
}
