/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore.PasswordProtection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;

/**
 * Multithreaded tests of {@link ExtendedKeyStoreService}.
 */
class MultithreadExtendedKeyStoreServiceIT {

	private static final char[] PASSWORD = "01234567890abcdefghijk".toCharArray();
	private static final String ALIAS = "myaliassymmetric-2_";

	private static void assertServiceIsThreadSafe(final KeyStoreService service) {
		final int size = Runtime.getRuntime().availableProcessors();
		final Collection<Callable<Void>> tasks = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			tasks.add(() -> invokeService(service, size * 2));
		}

		Collection<Future<Void>> futures;
		final ExecutorService executor = Executors.newFixedThreadPool(size);
		try {
			futures = executor.invokeAll(tasks);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		} finally {
			executor.shutdown();
		}

		try {
			for (final Future<Void> future : futures) {
				future.get();
			}
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new AssertionError(e);
		} catch (final ExecutionException e) {
			throw new AssertionError(e.getCause());
		}
	}

	private static Void invokeService(final KeyStoreService service, final int count) throws GeneralCryptoLibException, IOException {
		for (int i = 0; i < count; i++) {
			final CryptoAPIExtendedKeyStore store;
			try (final InputStream stream = MultithreadExtendedKeyStoreServiceIT.class.getResourceAsStream("/keystoreSymmetric.sks")) {
				store = service.loadKeyStore(stream, new PasswordProtection(PASSWORD));
			}
			for (int j = 0; j < 10; j++) {
				store.getSecretKeyEntry(ALIAS, PASSWORD);
			}
		}
		return null;
	}

	@Test
	void okWhenThreadSafeServicesTest() {
		final KeyStoreService service = new ExtendedKeyStoreService();
		assertServiceIsThreadSafe(service);
	}

	@Test
	void okWhenThreadSafeServicesUsedPoolTest() {
		final KeyStoreService service = new PollingExtendedKeyStoreServiceFactory().create();
		assertServiceIsThreadSafe(service);
	}

	@Test
	void okWhenThreadSafeServicesUsedPoolOf1Test() {
		final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(1);
		final KeyStoreService service = new PollingExtendedKeyStoreServiceFactory(config).create();
		assertServiceIsThreadSafe(service);
	}
}
