/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence;

/**
 * Contains the standard error codes used in the Online Voting products.
 */
public final class ErrorCodes {

	/* GENERAL ERRORS */
	/**
	 * The name of the error code mandatory field used in case a field does not appear.
	 */
	public static final String MANDATORY_FIELD = "mandatory_field";

	/**
	 * The name of the error missing parameter for query parameters.
	 */
	public static final String MISSING_QUERY_PARAMETER = "missing_query_parameter";

	/**
	 * The name of the error code when a NumberFormatException occurs".
	 */
	public static final String EXPECTED_NUMERIC_IDENTIFIER = "expected_numeric_identifier";

	/* RULE ERRORS */
	/**
	 * The name of the error code non existing type used in case a field does not appear.
	 */
	public static final String NON_EXISTING_TYPE = "non_existing_type";

	/* ELECTION EVENT ERRORS */
	/**
	 * The name of the error code election event not found used in case a field does not appear.
	 */
	public static final String ELECTION_EVENT_NOT_FOUND = "election_event_not_found";

	/* CONFIGURATION ERRORS */
	/**
	 * The name of the error code when a configuration fails in a update operation.
	 */
	public static final String CONFIGURATION_UPDATE_FAILED = "update_configuration_failed";

	/**
	 * The name of the error code when a configurations fails in its creation.
	 */
	public static final String CONFIGURATION_CREATION_FAILED = "create_configuration_failed";

	/* VALIDATION ERRORS */
	/**
	 * The name of the error code when a Exception occurs during validation".
	 */
	public static final String ERROR_IN_VALIDATION = "validation_failed";

	/* BALLOT ERRORS */
	/**
	 * The name of the error code when a Exception occurs during ballot creation".
	 */
	public static final String BALLOT_CREATION_FAILED = "ballot_creation_failed";

	/**
	 * The name of the error code when a Exception occurs when ballot is not found.
	 */
	public static final String BALLOT_NOT_FOUND = "ballot_not_found";

	/**
	 * Resource not found
	 */
	public static final String RESOURCE_NOT_FOUND = "resource_not_found";

	/**
	 * Duplicate entry
	 */
	public static final String DUPLICATE_ENTRY = "duplicate_entry";

	/**
	 * Valdiation error
	 */
	public static final String VALIDATION_EXCEPTION = "validation_error";

	// Avoid initialization.
	private ErrorCodes() {
	}
}
