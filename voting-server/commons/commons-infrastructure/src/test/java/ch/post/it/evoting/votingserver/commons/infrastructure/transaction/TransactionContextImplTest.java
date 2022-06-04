/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ejb.EJBContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionContextImplTest {

	private EJBContext context;

	@BeforeEach
	void setUp() {
		context = mock(EJBContext.class);
	}

	@Test
	void testIsRollbackOnly() {
		final TransactionContextImpl wrapper = new TransactionContextImpl(context, true);

		assertFalse(wrapper.isRollbackOnly());
		when(context.getRollbackOnly()).thenReturn(true);
		assertTrue(wrapper.isRollbackOnly());
	}

	@Test
	void testIsRollbackOnlyNonTransactional() {
		final TransactionContextImpl transactionContext = new TransactionContextImpl(context, false);

		assertThrows(IllegalStateException.class, transactionContext::isRollbackOnly);
	}

	@Test
	void testIsTransactional() {
		assertTrue(new TransactionContextImpl(context, true).isTransactional());
		assertFalse(new TransactionContextImpl(context, false).isTransactional());
	}

	@Test
	void testSetRollbackOnly() {
		new TransactionContextImpl(context, true).setRollbackOnly();

		verify(context).setRollbackOnly();
	}

	@Test
	void testSetRollbackOnlyNonTransactional() {
		final TransactionContextImpl transactionContext = new TransactionContextImpl(context, false);

		assertThrows(IllegalStateException.class, transactionContext::setRollbackOnly);
	}
}
