/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.transaction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.ApplicationException;

import org.junit.jupiter.api.Test;

class EJBRollbackPolicyTest {

	@Test
	void testImpliesRollbackChecked() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertFalse(policy.impliesRollback(new IOException("test")));
	}

	@Test
	void testImpliesRollbackRemote() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertTrue(policy.impliesRollback(new RemoteException()));
	}

	@Test
	void testImpliesRollbackUncheckedA() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertTrue(policy.impliesRollback(new ExceptionA()));
	}

	@Test
	void testImpliesRollbackUncheckedB() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertTrue(policy.impliesRollback(new ExceptionB()));
	}

	@Test
	void testImpliesRollbackUncheckedC() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertFalse(policy.impliesRollback(new ExceptionC()));
	}

	@Test
	void testImpliesRollbackUncheckedD() {
		EJBRollbackPolicy policy = EJBRollbackPolicy.getInstance();
		assertTrue(policy.impliesRollback(new ExceptionD()));
	}

	@ApplicationException(rollback = true)
	public static class ExceptionA extends RuntimeException {
		private static final long serialVersionUID = -2059792068032343479L;
	}

	public static class ExceptionB extends ExceptionA {
		private static final long serialVersionUID = -230438548958479539L;
	}

	@ApplicationException(inherited = false)
	public static class ExceptionC extends ExceptionB {
		private static final long serialVersionUID = 6761900516813693832L;
	}

	public static class ExceptionD extends ExceptionC {
		private static final long serialVersionUID = 4715666027348599771L;
	}
}
