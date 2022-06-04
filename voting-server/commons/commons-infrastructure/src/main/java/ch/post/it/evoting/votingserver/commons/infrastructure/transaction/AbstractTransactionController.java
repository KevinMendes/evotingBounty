/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * Basic abstract implementation of {@link TransactionController} to be used as a super class for implementations which are singleton stateless
 * session beans.
 * <p>
 * Because of some features of OpenEJB this class cannot be declared as EJB component itself because it is used in several applications which leads to
 * {@code org.apache.openejb.DuplicateDeploymentIdException} if the applications are deployed on the same TomEE instance. To avoid the problem an
 * application, say {@code Foo}, should use the following approach:
 *
 * <pre>
 * <code>
 * &#64;Singleton
 * &#64;Local(TransactionController.class)
 * &#64;ConcurrencyManagement(ConcurrencyManagementType.BEAN)
 * public class FooTransactionController extends AbstractTransactionController {
 * }
 *
 * public class FooComponent {
 *     &#64;EJB(beanName="FooTransactionController")
 *     private TransactionController controller;
 * }
 * </code>
 * </pre>
 * <p>
 * This class is thread safe.
 */
public abstract class AbstractTransactionController implements TransactionController {

	private EJBContext context;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public <T> T doInNewTransaction(final TransactionalAction<T> action) throws TransactionalActionException {
		return doInNewTransaction(action, EJBRollbackPolicy.getInstance());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@Override
	public <T> T doInNewTransaction(final TransactionalAction<T> action, final RollbackPolicy policy) throws TransactionalActionException {
		T result;
		try {
			result = action.execute(newTransactionContext(true));
		} catch (RuntimeException e) {
			if (policy.impliesRollback(e)) {
				context.setRollbackOnly();
			}
			throw e;
		} catch (Exception e) {
			if (policy.impliesRollback(e)) {
				context.setRollbackOnly();
			}
			throw new TransactionalActionException(e);
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public <T> T doInTransaction(final TransactionalAction<T> action) throws TransactionalActionException {
		return doInTransaction(action, EJBRollbackPolicy.getInstance());
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@Override
	public <T> T doInTransaction(final TransactionalAction<T> action, final RollbackPolicy policy) throws TransactionalActionException {
		T result;
		try {
			result = action.execute(newTransactionContext(true));
		} catch (RuntimeException e) {
			if (policy.impliesRollback(e)) {
				context.setRollbackOnly();
			}
			throw e;
		} catch (Exception e) {
			if (policy.impliesRollback(e)) {
				context.setRollbackOnly();
			}
			throw new TransactionalActionException(e);
		}
		return result;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	@Override
	public <T> T doOutOfTransaction(final TransactionalAction<T> action) throws TransactionalActionException {
		T result;
		try {
			result = action.execute(newTransactionContext(false));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new TransactionalActionException(e);
		}
		return result;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the context.
	 */
	@Resource
	public void setContext(final EJBContext context) {
		this.context = context;
	}

	private TransactionContext newTransactionContext(final boolean transactional) {
		return new TransactionContextImpl(context, transactional);
	}
}
