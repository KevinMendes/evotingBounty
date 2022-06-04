/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

/**
 * Transactional action.
 * <p>
 * <b>WARNING:</b> Implementations MUST NEITHER pass the provided instance of
 * {@link TransactionContext} to other transaction aware components NOR use it in different threads.
 */
public interface TransactionalAction<T> {
	/**
	 * Executes the action in the specified transaction context.
	 *
	 * @param context the context
	 * @return the action result
	 * @throws Exception the execution failed.
	 */
	T execute(TransactionContext context) throws TransactionalActionException;
}
