/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

/**
 * Rollback policy defines which exceptions cause transaction rollback.
 */
public interface RollbackPolicy {
	/**
	 * Returns whether the policy implies rollback in case of the specified exception.
	 *
	 * @param e the exception
	 * @return the policy implies rollback.
	 */
	boolean impliesRollback(Exception e);
}
