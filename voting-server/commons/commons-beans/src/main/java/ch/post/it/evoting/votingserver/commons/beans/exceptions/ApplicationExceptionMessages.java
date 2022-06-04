/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

/**
 * Contains all the exception messages.
 */
public final class ApplicationExceptionMessages {

	/**
	 * The exception message in case vote is null.
	 */
	public static final String EXCEPTION_MESSAGE_VOTE_IS_NULL = "Vote is null!";

	/**
	 * The exception election type is null.
	 */
	public static final String EXCEPTION_ELECTION_TYPE_IS_NULL = "Election type is null!";

	/**
	 * The exception election type is null.
	 */
	public static final String EXCEPTION_NON_EXISTING_ELECTION_TYPE = "Election type does not exist!";

	/**
	 * The exception message in case tenant id is null.
	 */
	public static final String EXCEPTION_MESSAGE_TENANT_ID_IS_NULL = "Tenant id is null!";

	/**
	 * The exception message in case election id is null.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_ID_IS_NULL = "Election id is null!";

	/**
	 * The exception message in case election event id is null.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL = "Election event id is null!";

	/**
	 * The exception message in case rule name is null.
	 */
	public static final String EXCEPTION_MESSAGE_RULE_NAME_IS_NULL = "Rule name is null!";

	/**
	 * The exception message in case rule not exists.
	 */

	public static final String EXCEPTION_MESSAGE_RULE_NOT_DEFINED = "Rule not defined in the system!";

	/**
	 * The exception message in case configuration not exists.
	 */
	public static final String EXCEPTION_MESSAGE_CONFIGURATION_NOT_DEFINED = "Configuration not defined in the system!";

	/**
	 * The name of the exception message in case an election event is null.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_EVENT_IS_NULL = "Election event is null!";

	/**
	 * The name of the exception message in case an election event is null.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_EVENT_NOT_FOUND = "Election event not found!";

	/**
	 * The name of the exception message in case an election is null.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_IS_NULL = "Election is null!";

	/**
	 * The name of the exception message in case an election does not exist.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_NOT_FOUND = "Election not found!";

	/**
	 * The name of the exception message in case of not having the elections defined for a ballot configuration.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTIONS_IS_EMPTY = "The set of elections is empty!";

	/**
	 * The name of the exception message in case a Contest does not exist.
	 */
	public static final String EXCEPTION_MESSAGE_CONTEST_NOT_FOUND = "Contest not found!";

	/**
	 * The name of the exception message in case of a Ballot is null.
	 */
	public static final String EXCEPTION_MESSAGE_BALLOT_IS_NULL = "Ballot is null!";

	/**
	 * The name of the exception message in case of a Ballot is not found.
	 */
	public static final String EXCEPTION_MESSAGE_BALLOT_NOT_FOUND = "Ballot not found!";

	/**
	 * The name of the exception message in case of a Ballot ID is null.
	 */
	public static final String EXCEPTION_MESSAGE_BALLOT_ID_IS_NULL = "Ballot ID is null!";

	/**
	 * The name of the exception message in case of a tenant id not corresponds to a ballot.
	 */
	public static final String EXCEPTION_MESSAGE_TENANT_ID_INCONSISTENT = "Tenant id is inconsistent with the configuration for this ballot!";

	/**
	 * The name of the exception message in case of a query parameter is null.
	 */
	public static final String EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL = "Query parameter can not be null!";

	/**
	 * The name of the exception message in case of a query parameter is null or invalid.
	 */
	public static final String EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL_OR_INVALID = "Query parameter is null or invalid!";

	/**
	 * The name of the exception message in case of a query parameter does not correspond to the received payload.
	 */
	public static final String EXCEPTION_MESSAGE_QUERY_PARAMETER_NOT_MATCH_PAYLOAD = "Query parameter does not correspond to the payload!";

	/**
	 * Exception message which is shown when there are more than one election event for a given tenant and id.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_EVENT_NOT_UNIQUE = "Election event error: there are more than one election event identified by the same id for this tenant!";

	/**
	 * Exception message which is shown when there are more than one election id and election event.
	 */
	public static final String EXCEPTION_MESSAGE_ELECTION_NOT_UNIQUE = "Election event error: there are more than one election identified by the same id and election event!";

	/**
	 * Exception message which is shown when there are more than one contest for a given election.
	 */
	public static final String EXCEPTION_MESSAGE_CONTEST_NOT_UNIQUE = "Contest error: there are more than one contest identified by the same id for this election!";

	/**
	 * Exception message which is shown when there are more than one ballot for a given tenant, and ballot id.
	 */
	public static final String EXCEPTION_MESSAGE_BALLOT_NOT_UNIQUE = "Configuration error: there are more than one ballot for the same tenant and ballot id!";

	public static final String EXCEPTION_MESSAGE_VOTING_CARD_ID_IS_NULL = "Voting Card Id is null!";

	public static final String EXCEPTION_MESSAGE_CREDENTIAL_ID_IS_NULL = "Credential Id is null!";

	public static final String EXCEPTION_MESSAGE_AUTH_TOKEN_INVALID_FORMAT = "Authentication Token has invalid format";

	/*
	 * Avoid instantiation.
	 */

	/**
	 * Instantiates a new application exception messages.
	 */
	private ApplicationExceptionMessages() {
	}
}
