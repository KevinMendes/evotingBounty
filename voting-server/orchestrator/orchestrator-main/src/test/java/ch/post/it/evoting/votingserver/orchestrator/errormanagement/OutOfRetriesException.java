/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.errormanagement;

/**
 * Sending the message was retried unsuccessfully too many times.
 */
public class OutOfRetriesException extends ManagedExecutionException {

	private static final long serialVersionUID = 9186672107632563661L;

	/**
	 * @param lastCause the last exception thrown before running out of retries
	 * @param retries   the number of times the action has been attempted before finally failing
	 */
	public OutOfRetriesException(Throwable lastCause, int retries) {
		super(String.format("The action did not succeed after %d retries", retries), lastCause);
	}
}
