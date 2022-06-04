/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.crypto.SecretKey;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.symmetric.utils.SymmetricTestDataGenerator;

/**
 * Multithreaded tests of {@link SymmetricService}.
 */
class MultithreadSymmetricServiceIT {
	private static final byte[] DATA = "dataToEncrypt".getBytes(StandardCharsets.UTF_8);

	private static SecretKey secretKey;

	@BeforeAll
	public static void setup() throws GeneralCryptoLibException {
		secretKey = SymmetricTestDataGenerator.getSecretKeyForEncryption();
	}

	private static void assertServiceIsThreadSafe(final SymmetricServiceAPI service) {
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

	private static Void invokeService(final SymmetricServiceAPI service, final int count) throws GeneralCryptoLibException {
		for (int i = 0; i < count; i++) {
			final byte[] encrypted = service.encrypt(secretKey, DATA);
			final byte[] decrypted = service.decrypt(secretKey, encrypted);
			if (!Arrays.equals(DATA, decrypted)) {
				throw new GeneralCryptoLibException("Data is corrupted.");
			}
		}
		return null;
	}

	@Test
	void okWhenThreadSafeServicesUsedPoolOf1Test() {
		final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(1);
		final SymmetricServiceAPI service = new PollingSymmetricServiceFactory(config).create();
		assertServiceIsThreadSafe(service);
	}

	@Test
	void usingHelperTest() throws GeneralCryptoLibException {
		final SymmetricServiceAPI service = SymmetricServiceFactoryHelper.getFactoryOfThreadSafeServices().create();
		assertServiceIsThreadSafe(service);
	}

	@Test
	void usingHelperWithParamsTest() throws GeneralCryptoLibException {
		final GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(1);
		final SymmetricServiceAPI service = SymmetricServiceFactoryHelper.getFactoryOfThreadSafeServices(config).create();
		assertServiceIsThreadSafe(service);
	}
}
