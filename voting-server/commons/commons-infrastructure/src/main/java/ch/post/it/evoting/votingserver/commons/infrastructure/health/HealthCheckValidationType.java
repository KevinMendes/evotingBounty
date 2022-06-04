/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.health;

/**
 * Enum class with the names of the different validations that can be performed in a call to a check health endpoint
 */
public enum HealthCheckValidationType {

	/**
	 * It will check if the connection to the database is working
	 */
	DATABASE("database"),
	/**
	 * Ot will check if the logging has been initialized in the service
	 */
	LOGGING_INITIALIZED("logging.init_state"),

	STATUS("status");

	private final String validationName;

	HealthCheckValidationType(final String validationName) {
		this.validationName = validationName;
	}

	/**
	 * Gets the validationName.
	 *
	 * @return Value of validationName.
	 */
	public String getValidationName() {
		return validationName;
	}
}
