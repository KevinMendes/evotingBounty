/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import javax.ejb.Local;

/**
 * Transaction controller is responsible for executing caller's actions in some transaction context.
 * <p>
 * <b>WARNING:</b> Implementations of {@link TransactionalAction} supplied to {@code doXXX} methods
 * MUST NOT use resources like JDBC or JMS which are already enlisted in different transaction.
 */
@Local
public interface TransactionController {
	/**
	 * Executes a given action in a new transaction. This is a shortcut for {@code doInNewTransaction(action, EJBRollbackPolicy.getInstance())}.
	 *
	 * @param action the action
	 * @return the action result
	 * @throws TransactionalActionException failed to execute the action.
	 */
	<T> T doInNewTransaction(TransactionalAction<T> action) throws TransactionalActionException;

	/**
	 * Executes a given action in a new transaction.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws checked exception then it is rethrown as a {@link
	 * TransactionalActionException} wrapping the original exception.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws unchecked exception then it will be propagated as is.
	 * <p>
	 * In case of exception the transaction is rolled back according the supplied rollback policy.
	 *
	 * @param action the action
	 * @param policy the policy
	 * @return the action result
	 * @throws TransactionalActionException the action threw a checked exception.
	 */
	<T> T doInNewTransaction(TransactionalAction<T> action, RollbackPolicy policy) throws TransactionalActionException;

	/**
	 * Executes a given action in transaction either existing or new. This is a shortcut for {@code doInTransaction(action,
	 * EJBRollbackPolicy.getInstance())}.
	 *
	 * @param action the action
	 * @return the action result
	 * @throws TransactionalActionException failed to execute the action.
	 */
	<T> T doInTransaction(TransactionalAction<T> action) throws TransactionalActionException;

	/**
	 * Executes a given action in transaction either existing or new.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws checked exception then it is rethrown as a {@link
	 * TransactionalActionException} wrapping the original exception.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws unchecked exception then it will be propagated as is.
	 * <p>
	 * In case of exception the transaction is rolled back according the supplied rollback policy.
	 *
	 * @param action the action
	 * @param policy the policy
	 * @return the action result
	 * @throws TransactionalActionException failed to execute the action.
	 */
	<T> T doInTransaction(TransactionalAction<T> action, RollbackPolicy policy) throws TransactionalActionException;

	/**
	 * Executes a given action out of transaction suspending the current transaction if exists.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws checked exception then it is rethrown as a {@link
	 * TransactionalActionException} wrapping the original exception.
	 * <p>
	 * If the {@link TransactionalAction#execute(TransactionContext)} throws unchecked exception then it will be propagated as is.
	 *
	 * @param action the action
	 * @return the action result
	 * @throws TransactionalActionException failed to execute the action.
	 */
	<T> T doOutOfTransaction(TransactionalAction<T> action) throws TransactionalActionException;
}
