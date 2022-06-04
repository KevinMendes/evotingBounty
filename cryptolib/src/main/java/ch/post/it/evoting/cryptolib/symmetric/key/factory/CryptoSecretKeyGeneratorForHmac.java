/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;

/**
 * A generator of secret keys to be used for HMAC.
 *
 * <p>Instances of this class are immutable.
 */
public final class CryptoSecretKeyGeneratorForHmac implements CryptoSecretKeyGenerator {

	private final String hmacSecretKeyAlgorithm;

	private final int hmacSecretKeyLength;

	private final PrimitivesServiceAPI primitivesService;

	CryptoSecretKeyGeneratorForHmac(final SymmetricKeyPolicy policy) {

		primitivesService = new PrimitivesService();

		hmacSecretKeyAlgorithm = policy.getHmacSecretKeyAlgorithmAndSpec().getAlgorithm();

		hmacSecretKeyLength = (policy.getHmacSecretKeyAlgorithmAndSpec().getKeyLengthInBits()) / Byte.SIZE;
	}

	@Override
	public SecretKey genSecretKey() {

		final byte[] randomBytes;
		try {
			randomBytes = primitivesService.genRandomBytes(hmacSecretKeyLength);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}

		return new SecretKeySpec(randomBytes, hmacSecretKeyAlgorithm);
	}
}
