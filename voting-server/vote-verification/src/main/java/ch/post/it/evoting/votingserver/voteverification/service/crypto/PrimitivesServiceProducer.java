/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service.crypto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesServiceFactoryHelper;

/**
 * Primitives Service Producer.
 */
public class PrimitivesServiceProducer {

	private static final String MAX_ELEMENTS_CRYPTO_POOL = "PRIMITIVES_MAX_ELEMENTS_CRYPTO_POOL";

	/**
	 * Returns a primitives service instance.
	 *
	 * @return a primitives service .
	 */
	@Produces
	@ApplicationScoped
	public PrimitivesServiceAPI getInstance() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		config.setMaxTotal(Integer.parseInt(System.getenv(MAX_ELEMENTS_CRYPTO_POOL)));
		try {
			return PrimitivesServiceFactoryHelper.getFactoryOfThreadSafeServices(config).create();
		} catch (GeneralCryptoLibException e) {
			throw new IllegalStateException("Exception while trying to create PrimitivesService", e);
		}
	}
}
