/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

/**
 * Transaction context.
 */
public interface TransactionContext {
	/**
	 * Returns whether the current transaction is set rollback only.
	 *
	 * @return the current transaction is marked rollback only.
	 * @throws IllegalStateException the context is not transactional.
	 */
	boolean isRollbackOnly();

	/**
	 * Returns whether the context is transactional i.e. is associated with a transaction.
	 *
	 * @return the context is transactional.
	 */
	boolean isTransactional();

	/**
	 * Sets the current transaction rollback only.
	 *
	 * @throws IllegalStateException the context is not transactional.
	 */
	void setRollbackOnly();
}
