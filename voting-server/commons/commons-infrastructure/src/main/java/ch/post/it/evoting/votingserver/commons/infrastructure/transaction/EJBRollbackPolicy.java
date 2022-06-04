/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import java.rmi.RemoteException;

import javax.ejb.ApplicationException;

/**
 * Implementation of {@link RollbackPolicy} which applies the rules defined in EJB specification 3.x.
 */
public final class EJBRollbackPolicy implements RollbackPolicy {
	private static final EJBRollbackPolicy INSTANCE = new EJBRollbackPolicy();

	private EJBRollbackPolicy() {
	}

	/**
	 * Returns the instance.
	 *
	 * @return the instance.
	 */
	public static EJBRollbackPolicy getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean impliesRollback(final Exception e) {
		boolean implies;
		if (e instanceof RemoteException) {
			implies = true;
		} else {
			Class<?> exceptionClass = e.getClass();
			ApplicationException annotation = exceptionClass.getDeclaredAnnotation(ApplicationException.class);
			if (annotation != null) {
				implies = annotation.rollback();
			} else {
				annotation = exceptionClass.getAnnotation(ApplicationException.class);
				if (annotation != null) {
					implies = annotation.inherited() && annotation.rollback();
				} else {
					implies = e instanceof RuntimeException;
				}
			}
		}
		return implies;
	}
}
