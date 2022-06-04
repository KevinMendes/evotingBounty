/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

/**
 * Exception for wrapping application exception thrown by some transactional action.
 */
public final class TransactionalActionException extends Exception {
	private static final long serialVersionUID = -3388579683095867784L;

	/**
	 * Constructor.
	 *
	 * @param cause
	 */
	public TransactionalActionException(final Throwable cause) {
		super(cause);
	}
}
