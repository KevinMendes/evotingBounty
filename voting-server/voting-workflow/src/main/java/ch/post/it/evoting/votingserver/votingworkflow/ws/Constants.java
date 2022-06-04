/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws;

/**
 * Class for constants.
 */
public final class Constants {

	/**
	 * The request id to be used for logging purposes.
	 */
	public static final String PARAMETER_HEADER_REQUEST_ID = "requestid";

	/**
	 * The length in characters of the random generated string.
	 */
	public static final int LENGTH_IN_CHARS = 16;

	private Constants() {
		// To avoid instantiation
	}
}
