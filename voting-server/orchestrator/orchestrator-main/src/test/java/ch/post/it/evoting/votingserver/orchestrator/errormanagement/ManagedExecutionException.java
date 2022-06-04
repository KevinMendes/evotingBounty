/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.errormanagement;

/**
 * A function whose execution is managed through a failure management policy has, alas, failed.
 */
public class ManagedExecutionException extends Exception {

	static final long serialVersionUID = 2352512312233L;

	public ManagedExecutionException(String message, Throwable cause) {
		super(message, cause);
	}
}
