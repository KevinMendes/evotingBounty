/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class that provides the functionality for password based key derivation. This class implements {@link CryptoAPIPBKDFDeriver}
 *
 * <p>Note that the {@link SecretKeyFactory} that is used to generate the derived password, as well
 * as the {@link PrimitivesServiceAPI}, which is used to generate the random bytes, are stored in this class and cannot be modified.
 */
public final class CryptoPBKDFDeriver implements CryptoAPIPBKDFDeriver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptoPBKDFDeriver.class);

	private static final String KEY_DERIVATION_ERROR = "Error while deriving the key";

	private final SecretKeyFactory secretKeyFactory;

	private final PrimitivesServiceAPI primitivesService;

	private final int iterations;

	private final int keyLength;

	private final int saltBytesLength;

	private final int minPasswordLength;

	private final int maxPasswordLength;

	/**
	 * Constructs a new CryptoPBKDFDeriver with the specified value.
	 *
	 * @param secretKeyFactory  an already initialized secret key factory.
	 * @param primitivesService the primitives service used to generate the salt.
	 * @param saltBitLength     the length in bits of the salt.
	 * @param iterations        the number of iterations to be performed while deriving the password.
	 * @param keyLength         the length in bytes of the derived key.
	 * @param minPasswordLength the minimum length of the password to derive.
	 * @param maxPasswordLength The maximum length of the password to derive.
	 * @throws GeneralCryptoLibException if the salt bit length is invalid.
	 */
	CryptoPBKDFDeriver(final SecretKeyFactory secretKeyFactory, final PrimitivesServiceAPI primitivesService, final int saltBitLength,
			final int iterations, final int keyLength, final int minPasswordLength, final int maxPasswordLength) throws GeneralCryptoLibException {

		this.secretKeyFactory = secretKeyFactory;

		this.primitivesService = primitivesService;

		this.iterations = iterations;

		this.keyLength = keyLength;

		this.minPasswordLength = minPasswordLength;

		this.maxPasswordLength = maxPasswordLength;
		if (saltBitLength % Byte.SIZE != 0) {
			throw new CryptoLibException("The salt bit lengths is not a multiple of the number of bits per byte.");
		}

		Validate.isPositive(saltBitLength, "Salt length in bits");

		saltBytesLength = saltBitLength / Byte.SIZE;
	}

	/**
	 * Derives a {@link CryptoAPIDerivedKey} from a given password and a given salt. The password must contain a minimum of 16 characters and a
	 * maximum of 1000 characters.
	 *
	 * @param password the password.
	 * @param salt     the salt.
	 * @return the derived {@link CryptoAPIDerivedKey}.
	 * @throws GeneralCryptoLibException if the password or the salt are invalid.
	 */
	@Override
	public CryptoAPIDerivedKey deriveKey(final char[] password, final byte[] salt) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(password, "Password");
		Validate.inRange(password.length, minPasswordLength, maxPasswordLength, "Password length", "", "");
		Validate.notNullOrEmpty(salt, "Salt");

		final PBEKeySpec pbeKeySpec = new PBEKeySpec(password, salt, iterations, keyLength);

		final SecretKey key;
		try {
			key = secretKeyFactory.generateSecret(pbeKeySpec);
		} catch (final InvalidKeySpecException e) {
			throw new CryptoLibException(KEY_DERIVATION_ERROR, e);
		}
		final byte[] encoded = key.getEncoded();
		// The purpose of the code below is to prevent the JIT compiler from
		// eliminating the key instance using scalar replacement optimization.
		// Otherwise it can lead to unpredictable errors when the key content
		// is an array of zeros. See https://www.stefankrause.net/wp/?p=64
		// (accessed 2020-05-17) for details.
		if (Arrays.equals(encoded, new byte[encoded.length])) {
			LOGGER.error("Derived key ''{}'' is probably corrupted.", key);
		}
		return new CryptoDerivedKey(encoded);
	}

	public byte[] generateRandomSalt() {
		try {
			return primitivesService.genRandomBytes(getSaltBytesLength());
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}

	public int getSaltBytesLength() {
		return saltBytesLength;
	}
}
