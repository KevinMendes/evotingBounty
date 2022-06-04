/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.cryptolib.symmetric.cipher.factory;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.commons.binary.ByteArrayBuilder;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.EncryptionOperatingMode;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;

/**
 * A symmetric cipher using authenticated encryption.
 *
 * <p>Instances of this class are immutable.
 */
public class SymmetricAuthenticatedCipher {

	private final PrimitivesServiceAPI primitivesService;
	private final String algorithm;
	private final EncryptionOperatingMode mode;
	private final int initVectorByteLength;
	private final int authTagBitLength;
	private final Cipher cipher;

	/**
	 * Creates an instance of a symmetric authenticated cipher, using the specified policy.
	 *
	 * @param symmetricCipherPolicy the policy for symmetric ciphers.
	 * @throws GeneralCryptoLibException if the creation of the symmetric cipher instance fails.
	 */
	SymmetricAuthenticatedCipher(final SymmetricCipherPolicy symmetricCipherPolicy) throws GeneralCryptoLibException {

		// Create symmetric cipher instance.
		final ConfigSymmetricCipherAlgorithmAndSpec symmetricCipherAlgorithmAndSpec = symmetricCipherPolicy.getSymmetricCipherAlgorithmAndSpec();
		final String transformation = symmetricCipherAlgorithmAndSpec.getTransformation();
		final String provider = symmetricCipherAlgorithmAndSpec.getProvider().getProviderName();
		try {
			if (Provider.DEFAULT.equals(symmetricCipherAlgorithmAndSpec.getProvider())) {
				cipher = Cipher.getInstance(transformation);
			} else {
				cipher = Cipher.getInstance(transformation, provider);
			}
		} catch (final GeneralSecurityException e) {
			final String errorMessage = String.format("Failed to create symmetric cipher in this environment. Attempted to use the provider: %s and the "
					+ "algorithm/mode/padding: %s. Error message was %s.", provider, transformation, e.getMessage());
			throw new GeneralCryptoLibException(errorMessage, e);
		}

		// Create instance of random number generator for creating
		// initialization vector.
		primitivesService = new PrimitivesService();

		// Retrieve cipher algorithm and encryption operating mode.
		algorithm = symmetricCipherAlgorithmAndSpec.getAlgorithm();
		mode = symmetricCipherAlgorithmAndSpec.getMode();

		// Retrieve initialization vector byte length. If this property is set
		// to zero by policy, then retrieve it from the cipher itself.
		final int initVectorBitLengthFromPolicy = symmetricCipherAlgorithmAndSpec.getInitVectorBitLength();
		if (initVectorBitLengthFromPolicy > 0) {
			initVectorByteLength = initVectorBitLengthFromPolicy / Byte.SIZE;
		} else {
			initVectorByteLength = cipher.getBlockSize();
		}
		if (initVectorByteLength <= 0) {
			final String errorMessage = String.format("Initialization vector byte length %s is invalid.", initVectorByteLength);
			throw new GeneralCryptoLibException(errorMessage);
		}

		// Retrieve authentication tag bit length.
		authTagBitLength = symmetricCipherAlgorithmAndSpec.getAuthTagBitLength();
		if (authTagBitLength <= 0) {
			final String errorMessage = String.format("Authentication tag bit length %s must be greater than 0.", authTagBitLength);
			throw new GeneralCryptoLibException(errorMessage);
		}
	}

	/**
	 * Uses a authenticated symmetric cipher to encrypt the given message using the given {@link javax.crypto.SecretKey}.
	 *
	 * @param secretKey the {@link javax.crypto.SecretKey} to use.
	 * @param message   the variable-length message to be encrypted
	 * @return the byte array concatenation of the initialization vector and authenticated ciphertext
	 * @throws GeneralCryptoLibException if the input validation or the cipher initialization fails
	 */
	public byte[] genAuthenticatedEncryption(final SecretKey secretKey, final byte[] message) throws GeneralCryptoLibException {

		validateSecretKeyAlgorithm(secretKey);

		final ByteArrayBuilder initializationVectorAndAuthenticatedCiphertext = new ByteArrayBuilder();

		// Generate initialization vector.
		final byte[] initVector = primitivesService.genRandomBytes(initVectorByteLength);
		initializationVectorAndAuthenticatedCiphertext.append(initVector);

		// Initialize authenticated symmetric cipher for encryption
		try {
			if (EncryptionOperatingMode.GCM.equals(mode)) {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(authTagBitLength, initVector));
			} else {
				final String errorMessage = String
						.format("Encryption operating mode '%s' is not a recognized mode for use with authentication tag generation.", mode);
				throw new CryptoLibException(errorMessage);
			}
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not initialize symmetric cipher for encryption.", e);
		}

		// Generate authenticated encryption of plaintext message
		try {
			initializationVectorAndAuthenticatedCiphertext.append(cipher.doFinal(message));
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not symmetrically encrypt message.", e);
		}

		return initializationVectorAndAuthenticatedCiphertext.build();
	}

	/**
	 * Uses a authenticated symmetric cipher to decrypt the provided ciphertext using the provided SecretKey.
	 *
	 * @param secretKey                                      the {@link javax.crypto.SecretKey} to use.
	 * @param initializationVectorAndAuthenticatedCiphertext the byte array concatenation of the initialization vector and the encrypted data.
	 * @return the plaintext message.
	 * @throws GeneralCryptoLibException if the input validation or the cipher initialization fails.
	 */
	public byte[] getAuthenticatedDecryption(final SecretKey secretKey, final byte[] initializationVectorAndAuthenticatedCiphertext)
			throws GeneralCryptoLibException {

		validateSecretKeyAlgorithm(secretKey);

		validateAuthenticatedCiphertextLength(initializationVectorAndAuthenticatedCiphertext);

		// Retrieve initialization vector and authenticated ciphertext.
		final byte[] initVector = getInitVector(initializationVectorAndAuthenticatedCiphertext);
		final byte[] authenticatedCiphertext = getAuthenticatedCiphertext(initializationVectorAndAuthenticatedCiphertext);

		// Initialize cipher for authenticated decryption.
		try {
			if (EncryptionOperatingMode.GCM.equals(mode)) {
				cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(authTagBitLength, initVector));
			} else {
				final String errorMessage = String
						.format("Decryption operating mode '%s' is not a recognized mode for use with authentication tag generation.", mode);
				throw new CryptoLibException(errorMessage);
			}
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not initialize symmetric cipher for decryption.", e);
		}

		// Verify authentication tag, then decrypt data.
		try {
			return cipher.doFinal(authenticatedCiphertext);
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not symmetrically decrypt authenticated message.", e);
		}
	}

	/**
	 * Retrieves the initialization vector from the byte array concatenation of the initialization vector and the encrypted data.
	 *
	 * @param initializationVectorAndAuthenticatedCiphertext the byte array concatenation of the initialization vector and the encrypted data.
	 * @return the initialization vector.
	 */
	byte[] getInitVector(final byte[] initializationVectorAndAuthenticatedCiphertext) {

		return Arrays.copyOfRange(initializationVectorAndAuthenticatedCiphertext, 0, initVectorByteLength);
	}

	/**
	 * Retrieves the authenticated ciphertext (consisting of the concatenation of the ciphertext and the authentication tag) from the byte array
	 * concatenation of the initialization vector and the encrypted data.
	 *
	 * @param initializationVectorAndAuthenticatedCiphertext the byte array concatenation of the initialization vector and the authenticated
	 *                                                       ciphertext.
	 * @return the authenticated ciphertext.
	 */
	private byte[] getAuthenticatedCiphertext(final byte[] initializationVectorAndAuthenticatedCiphertext) {

		return Arrays.copyOfRange(initializationVectorAndAuthenticatedCiphertext, initVectorByteLength,
				initializationVectorAndAuthenticatedCiphertext.length);
	}

	/**
	 * Validates the secret key algorithm against the symmetric cipher algorithm.
	 *
	 * @param key the {@link javax.crypto.SecretKey} for which to check the algorithm.
	 * @throws GeneralCryptoLibException if the secret key algorithm does not match the symmetric cipher algorithm.
	 */
	private void validateSecretKeyAlgorithm(final SecretKey key) throws GeneralCryptoLibException {

		final String secretKeyAlgorithm = key.getAlgorithm();

		if (!secretKeyAlgorithm.equals(algorithm)) {
			final String errorMsg = String
					.format("Secret key algorithm '%s' does not match symmetric cipher algorithm '%s'.", secretKeyAlgorithm, algorithm);
			throw new GeneralCryptoLibException(errorMsg);
		}
	}

	private void validateAuthenticatedCiphertextLength(final byte[] initializationVectorAndAuthenticatedCiphertext) throws GeneralCryptoLibException {

		if (initializationVectorAndAuthenticatedCiphertext.length <= initVectorByteLength) {
			final String errorMessage = String
					.format("Byte length %s of byte array concatenation of initialization vector and authenticated ciphertext is less than or "
									+ "equal to byte length %s of initialization vector.", initializationVectorAndAuthenticatedCiphertext.length,
							initVectorByteLength);
			throw new GeneralCryptoLibException(errorMessage);
		}
	}
}
