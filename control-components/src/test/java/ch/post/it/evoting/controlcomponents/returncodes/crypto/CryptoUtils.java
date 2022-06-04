/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.controlcomponents.returncodes.crypto;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesServiceFactoryHelper;

public class CryptoUtils {

	private static final PrimitivesServiceAPI primitivesService;

	static {
		GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
		genericObjectPoolConfig.setMaxTotal(50);
		genericObjectPoolConfig.setMaxIdle(50);
		ServiceFactory<PrimitivesServiceAPI> primitivesServiceAPIServiceFactory = PrimitivesServiceFactoryHelper
				.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
		try {
			primitivesService = primitivesServiceAPIServiceFactory.create();
		} catch (GeneralCryptoLibException e) {
			throw new CryptoLibException("Exception while trying to create PrimitivesService", e);
		}
	}

	private CryptoUtils() {
	}

	public static CryptoAPIKDFDeriver getKDFDeriver() {
		return primitivesService.getKDFDeriver();
	}
}
