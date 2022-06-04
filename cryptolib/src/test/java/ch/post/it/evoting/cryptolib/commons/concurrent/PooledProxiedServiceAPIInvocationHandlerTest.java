/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import static java.lang.System.identityHashCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Proxy;

import org.apache.commons.pool2.ObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PooledProxiedServiceAPIInvocationHandlerTest {

	private static final String INPUT = "input";
	private static final String OUTPUT = "output";

	private PooledProxiedServiceAPIInvocationHandler<TestService> handler;
	private TestService proxy;

	@Mock
	private TestService service;

	@Mock
	private ObjectPool<TestService> pool;

	@BeforeEach
	void setUp() {
		handler = new PooledProxiedServiceAPIInvocationHandler<>(pool);
		final ClassLoader loader = TestService.class.getClassLoader();
		final Class<?>[] interfaces = new Class<?>[] { TestService.class };
		proxy = (TestService) Proxy.newProxyInstance(loader, interfaces, handler);
	}

	@Test
	void testInvokeEquals() {
		assertEquals(proxy, proxy);
		final ClassLoader loader = TestService.class.getClassLoader();
		final Class<?>[] interfaces = new Class<?>[] { TestService.class };
		final TestService otherProxy = (TestService) Proxy.newProxyInstance(loader, interfaces, handler);

		assertNotEquals(proxy, otherProxy);
	}

	@Test
	void testInvokeHashCode() {
		assertEquals(identityHashCode(proxy), proxy.hashCode());
	}

	@Test
	void testInvokeToString() {
		assertTrue(proxy.toString().contains(proxy.getClass().getName()));
	}

	@Test
	void testInvokeCall() throws Exception {
		when(service.call(INPUT)).thenReturn(OUTPUT);
		when(pool.borrowObject()).thenReturn(service);

		assertEquals(OUTPUT, proxy.call(INPUT));

		verify(pool).returnObject(service);
		verify(pool, never()).invalidateObject(service);
	}

	@Test
	void testInvokeCallIOException() throws Exception {
		when(service.call(INPUT)).thenThrow(new IOException("test"));
		when(pool.borrowObject()).thenReturn(service);

		assertThrows(IOException.class, () -> proxy.call(INPUT));

		verify(pool, never()).returnObject(service);
		verify(pool).invalidateObject(service);
	}

	@Test
	void testInvokeCallSecurityException() throws Exception {
		when(service.call(INPUT)).thenThrow(new SecurityException("test"));
		when(pool.borrowObject()).thenReturn(service);

		assertThrows(SecurityException.class, () -> proxy.call(INPUT));

		verify(pool, never()).returnObject(service);
		verify(pool).invalidateObject(service);
	}

	@Test
	void testInvokeAquireFailed() throws Exception {
		when(pool.borrowObject()).thenThrow(new IOException("test"));

		assertThrows(IllegalStateException.class, () -> proxy.call(INPUT));

		verify(service, never()).call(INPUT);
		verify(pool, never()).returnObject(service);
		verify(pool, never()).invalidateObject(service);
	}

	@Test
	void testInvokeReleasePartiallyFailed() throws Exception {
		when(service.call(INPUT)).thenReturn(OUTPUT);
		when(pool.borrowObject()).thenReturn(service);
		doThrow(new IOException("test")).when(pool).returnObject(service);

		assertThrows(IllegalStateException.class, () -> proxy.call(INPUT));

		verify(pool).invalidateObject(service);
	}

	@Test
	void testInvokeReleaseFullyFailed() throws Exception {
		doThrow(new IOException("test")).when(pool).returnObject(service);
		doThrow(new IOException("test")).when(pool).invalidateObject(service);

		assertThrows(IllegalStateException.class, () -> proxy.call(INPUT));
	}

	@Test
	void testInvokeDestroyFailed() throws Exception {
		when(service.call(INPUT)).thenThrow(new IOException("test"));
		doThrow(new IOException("test")).when(pool).invalidateObject(service);

		assertThrows(IllegalStateException.class, () -> proxy.call(INPUT));
	}

	interface TestService {
		String call(String input) throws IOException;

		@Override
		boolean equals(Object other);
	}
}
