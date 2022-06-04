/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import javax.ejb.EJBContext;

/**
 * Implementation of {@link TransactionContext}.
 */
class TransactionContextImpl implements TransactionContext {
	private final EJBContext context;

	private final boolean transactional;

	/**
	 * Constructor.
	 *
	 * @param context
	 * @param transactional
	 */
	public TransactionContextImpl(final EJBContext context, final boolean transactional) {
		this.context = context;
		this.transactional = transactional;
	}

	@Override
	public boolean isRollbackOnly() {
		checkTransactional();
		return context.getRollbackOnly();
	}

	@Override
	public boolean isTransactional() {
		return transactional;
	}

	@Override
	public void setRollbackOnly() {
		checkTransactional();
		context.setRollbackOnly();
	}

	private void checkTransactional() {
		if (!isTransactional()) {
			throw new IllegalStateException("Context is not transactional.");
		}
	}
}
