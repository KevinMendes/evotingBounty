/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.crypto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreServiceFactoryHelper;

/**
 * Producer for extended keystore store service.
 */
public class StoresServiceProducer {

	private static final String MAX_ELEMENTS_CRYPTO_POOL = "STORES_MAX_ELEMENTS_CRYPTO_POOL";

	/**
	 * Returns a extended keystore store service.
	 *
	 * @return a extended keystore store service instance.
	 */
	@Produces
	@ApplicationScoped
	public KeyStoreService getInstance() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(Integer.parseInt(System.getenv(MAX_ELEMENTS_CRYPTO_POOL)));
		try {
			return ExtendedKeyStoreServiceFactoryHelper.getFactoryOfThreadSafeServices(config).create();
		} catch (GeneralCryptoLibException e) {
			throw new IllegalStateException("Exception while trying to create a ExtendedKeyStoreService", e);
		}
	}
}
