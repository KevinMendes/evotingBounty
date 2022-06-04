/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service.crypto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricServiceFactoryHelper;

/**
 * Symmetric Service Producer.
 */
public class SymmetricServiceProducer {

	private static final String MAX_ELEMENTS_CRYPTO_POOL = "SYMMETRIC_MAX_ELEMENTS_CRYPTO_POOL";

	/**
	 * Returns a symmetric service instance.
	 *
	 * @return a symmetric service.
	 */
	@Produces
	@ApplicationScoped
	public SymmetricServiceAPI getInstance() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(Integer.parseInt(System.getenv(MAX_ELEMENTS_CRYPTO_POOL)));
		try {
			return SymmetricServiceFactoryHelper.getFactoryOfThreadSafeServices(config).create();
		} catch (GeneralCryptoLibException e) {
			throw new IllegalStateException("Exception while trying to create SymmetricService", e);
		}
	}
}
