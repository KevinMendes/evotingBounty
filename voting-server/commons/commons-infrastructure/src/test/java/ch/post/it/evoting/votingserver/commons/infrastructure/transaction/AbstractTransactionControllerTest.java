/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AbstractTransactionControllerTest {

	private static final Exception SYSTEM_EXCEPTION = new EJBException("test");
	private static final Exception TRANSACTIONAL_ACTION_EXCEPTION = new TransactionalActionException(new Exception("test"));

	private EJBContext context;
	private TransactionalAction<Boolean> action;
	private RollbackPolicy policy;
	private AbstractTransactionController controller;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() throws Exception {
		context = mock(EJBContext.class);
		action = mock(TransactionalAction.class);
		when(action.execute(any(TransactionContext.class))).thenReturn(true);
		policy = mock(RollbackPolicy.class);
		when(policy.impliesRollback(SYSTEM_EXCEPTION)).thenReturn(true);
		when(policy.impliesRollback(TRANSACTIONAL_ACTION_EXCEPTION)).thenReturn(false);
		controller = new TestableTransactionController();
		controller.setContext(context);
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfT() throws TransactionalActionException {
		assertTrue(controller.doInNewTransaction(action));
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfTApplicationException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(TRANSACTIONAL_ACTION_EXCEPTION);

		final TransactionalActionException exception = assertThrows(TransactionalActionException.class, () -> controller.doInNewTransaction(action));
		assertEquals(TRANSACTIONAL_ACTION_EXCEPTION, exception.getCause());
		verify(context, never()).setRollbackOnly();
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfTRollbackPolicy() throws TransactionalActionException {
		assertTrue(controller.doInNewTransaction(action, policy));
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfTRollbackPolicyEJBException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(SYSTEM_EXCEPTION);

		final EJBException exception = assertThrows(EJBException.class, () -> controller.doInNewTransaction(action, policy));
		assertEquals(SYSTEM_EXCEPTION, exception);
		verify(context).setRollbackOnly();
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfTRollbackPolicyIOException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(TRANSACTIONAL_ACTION_EXCEPTION);

		final TransactionalActionException exception = assertThrows(TransactionalActionException.class,
				() -> controller.doInNewTransaction(action, policy));
		assertEquals(TRANSACTIONAL_ACTION_EXCEPTION, exception.getCause());
		verify(context, never()).setRollbackOnly();
	}

	@Test
	void testDoInNewTransactionTransactionalActionOfTSystemException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(SYSTEM_EXCEPTION);

		final EJBException exception = assertThrows(EJBException.class, () -> controller.doInNewTransaction(action));
		assertEquals(SYSTEM_EXCEPTION, exception);
		verify(context).setRollbackOnly();
	}

	@Test
	void testDoInTransactionTransactionalActionOfT() throws TransactionalActionException {
		assertTrue(controller.doInTransaction(action));
	}

	@Test
	void testDoInTransactionTransactionalActionOfTApplicationException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(TRANSACTIONAL_ACTION_EXCEPTION);

		final TransactionalActionException exception = assertThrows(TransactionalActionException.class, () -> controller.doInTransaction(action));
		assertEquals(TRANSACTIONAL_ACTION_EXCEPTION, exception.getCause());
		verify(context, never()).setRollbackOnly();
	}

	@Test
	void testDoInTransactionTransactionalActionOfTRollbackPolicy() throws TransactionalActionException {
		assertTrue(controller.doInTransaction(action, policy));
	}

	@Test
	void testDoInTransactionTransactionalActionOfTRollbackPolicyApplicationException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(TRANSACTIONAL_ACTION_EXCEPTION);

		final TransactionalActionException exception = assertThrows(TransactionalActionException.class,
				() -> controller.doInTransaction(action, policy));
		assertEquals(TRANSACTIONAL_ACTION_EXCEPTION, exception.getCause());
		verify(context, never()).setRollbackOnly();
	}

	@Test
	void testDoInTransactionTransactionalActionOfTRollbackPolicySystemException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(SYSTEM_EXCEPTION);

		final EJBException exception = assertThrows(EJBException.class, () -> controller.doInTransaction(action, policy));
		assertEquals(SYSTEM_EXCEPTION, exception);
		verify(context).setRollbackOnly();
	}

	@Test
	void testDoInTransactionTransactionalActionOfTSystemException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(SYSTEM_EXCEPTION);

		final EJBException exception = assertThrows(EJBException.class, () -> controller.doInTransaction(action));
		assertEquals(SYSTEM_EXCEPTION, exception);
		verify(context).setRollbackOnly();
	}

	@Test
	void testDoOutOfTransaction() throws TransactionalActionException {
		assertTrue(controller.doOutOfTransaction(action));
	}

	@Test
	void testDoOutOfTransactionApplicationException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(TRANSACTIONAL_ACTION_EXCEPTION);

		final TransactionalActionException exception = assertThrows(TransactionalActionException.class, () -> controller.doOutOfTransaction(action));
		assertEquals(TRANSACTIONAL_ACTION_EXCEPTION, exception.getCause());
	}

	@Test
	void testDoOutOfTransactionSystemException() throws Exception {
		when(action.execute(any(TransactionContext.class))).thenThrow(SYSTEM_EXCEPTION);

		final EJBException exception = assertThrows(EJBException.class, () -> controller.doOutOfTransaction(action));
		assertEquals(SYSTEM_EXCEPTION, exception);
	}

	private static class TestableTransactionController extends AbstractTransactionController {
	}
}
