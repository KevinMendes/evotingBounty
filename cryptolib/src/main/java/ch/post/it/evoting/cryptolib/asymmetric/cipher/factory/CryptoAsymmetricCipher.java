/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.factory;

import static ch.post.it.evoting.cryptolib.asymmetric.cipher.constants.AsymmetricCipherConstants.CIPHER_SPECIFICATION_DELIMITER;
import static ch.post.it.evoting.cryptolib.asymmetric.cipher.constants.AsymmetricCipherConstants.ENCRYPTION_MODE_FIELD_INDEX;
import static ch.post.it.evoting.cryptolib.asymmetric.cipher.constants.AsymmetricCipherConstants.RSA_KEM;
import static ch.post.it.evoting.cryptolib.asymmetric.cipher.constants.AsymmetricCipherConstants.RSA_KEM_WITH_KDF1_AND_SHA256;
import static ch.post.it.evoting.cryptolib.asymmetric.cipher.constants.AsymmetricCipherConstants.RSA_KEM_WITH_KDF2_AND_SHA256;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.KDF1BytesGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.kems.RSAKeyEncapsulation;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.AsymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.commons.binary.ByteArrays;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipher;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;

/**
 * An asymmetric cipher. This class aids in the process of performing asymmetric cryptographic operations such as encrypting or decrypting. Instances
 * of this class are immutable.
 */
public class CryptoAsymmetricCipher {

	private final String encryptionMode;

	private final String secretKeyAlgorithm;

	private final int secretKeyByteLength;

	private final SymmetricAuthenticatedCipher symmetricCipher;

	private Cipher cipher;

	private RSAKeyEncapsulation keyEncapsulation;

	/**
	 * Creates an instance of an asymmetric cipher, using the given policy.
	 *
	 * @param asymmetricCipherPolicy Policy for asymmetric ciphers.
	 * @throws GeneralCryptoLibException if an unsupported cipher algorithm, encryption mode, padding or cryptographic service provider is requested.
	 */
	CryptoAsymmetricCipher(final AsymmetricCipherPolicy asymmetricCipherPolicy) throws GeneralCryptoLibException {

		final String algorithmModePadding = asymmetricCipherPolicy.getAsymmetricCipherAlgorithmAndSpec().getAlgorithmModePadding();

		final String[] algorithmModePaddingFields = algorithmModePadding.split(CIPHER_SPECIFICATION_DELIMITER);
		encryptionMode = algorithmModePaddingFields[ENCRYPTION_MODE_FIELD_INDEX];

		final Provider provider = asymmetricCipherPolicy.getAsymmetricCipherAlgorithmAndSpec().getProvider();

		secretKeyAlgorithm = asymmetricCipherPolicy.getSecretKeyAlgorithmAndSpec().getAlgorithm();

		secretKeyByteLength = asymmetricCipherPolicy.getSecretKeyAlgorithmAndSpec().getKeyLength() / Byte.SIZE;

		symmetricCipher = new SymmetricAuthenticatedCipherFactory(asymmetricCipherPolicy).create();

		if (encryptionMode.startsWith(RSA_KEM)) {
			if (provider == Provider.BOUNCY_CASTLE || provider == Provider.DEFAULT) {
				final DerivationFunction derivationFunction;
				if (encryptionMode.equals(RSA_KEM_WITH_KDF1_AND_SHA256)) {
					derivationFunction = new KDF1BytesGenerator(new SHA256Digest());
				} else if (encryptionMode.equals(RSA_KEM_WITH_KDF2_AND_SHA256)) {
					derivationFunction = new KDF2BytesGenerator(new SHA256Digest());
				} else {
					throw new GeneralCryptoLibException(
							"Encryption mode '" + encryptionMode + "' is not supported by the asymmetric cipher cryptographic policy.");
				}

				final SecureRandom secureRandom = new SecureRandomFactory(asymmetricCipherPolicy).createSecureRandom();

				keyEncapsulation = new RSAKeyEncapsulation(derivationFunction, secureRandom);
			} else {
				throw new GeneralCryptoLibException(
						"Provider '" + provider.getProviderName() + "' is not supported by the asymmetric cipher cryptographic policy for use with '"
								+ RSA_KEM + "'");
			}
		} else {
			try {
				if (provider == Provider.DEFAULT) {
					cipher = Cipher.getInstance(algorithmModePadding);
				} else {
					cipher = Cipher.getInstance(algorithmModePadding, provider.getProviderName());
				}
			} catch (final GeneralSecurityException e) {
				throw new GeneralCryptoLibException(
						"Could not create asymmetric cipher instance for algorithm/mode/padding '" + algorithmModePadding + "' and provider '"
								+ provider + "'", e);
			}
		}
	}

	/**
	 * Asymmetrically encrypts some data.
	 *
	 * @param publicKey the {@link java.security.PublicKey} to use.
	 * @param data      the data to be encrypted.
	 * @return the encrypted data. For RSA-KEM encryption mode, this method returns the bitwise concatenation of the encapsulated derived secret key
	 * and the symmetrical encryption of the data with this secret key.
	 * @throws GeneralCryptoLibException if the given public key or data to encrypt is invalid, or if the encryption process fails.
	 */
	public byte[] encrypt(final PublicKey publicKey, final byte[] data) throws GeneralCryptoLibException {

		if (encryptionMode.startsWith(RSA_KEM)) {
			final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
			final int encapsulationByteLength = rsaPublicKey.getModulus().bitLength() / Byte.SIZE;
			final byte[] encapsulatedSecretKey = new byte[encapsulationByteLength];
			final SecretKey secretKey = deriveAndEncapsulateSecretKey(rsaPublicKey, encapsulatedSecretKey);
			return ByteArrays.concatenate(encapsulatedSecretKey, encrypt(secretKey, data));
		} else {
			initCipherForEncryption(publicKey);

			return encrypt(data);
		}
	}

	/**
	 * Asymmetrically decrypts some encrypted data.
	 *
	 * @param privateKey the {@link java.security.PrivateKey} to use.
	 * @param data       the encrypted data. For RSA-KEM encryption mode, this parameter consists of the bitwise concatenation of the encapsulated
	 *                   secret key and the encrypted data.
	 * @return the decrypted data.
	 * @throws GeneralCryptoLibException if the given private key or data to decrypt is invalid, or if the decryption process fails.
	 */
	public byte[] decrypt(final PrivateKey privateKey, final byte[] data) throws GeneralCryptoLibException {
		if (encryptionMode.startsWith(RSA_KEM)) {
			final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
			final int encapsulationByteLength = rsaPrivateKey.getModulus().bitLength() / Byte.SIZE;

			if (data.length < encapsulationByteLength) {
				throw new GeneralCryptoLibException("Data is not a valid RSA-KEM ciphertext.");
			}

			final SecretKey secretKey = decapsulateSecretKey(rsaPrivateKey, data, encapsulationByteLength);

			final byte[] encryptedData = Arrays.copyOfRange(data, encapsulationByteLength, data.length);

			return decrypt(secretKey, encryptedData);
		} else {
			initCipherForDecryption(privateKey);

			return decrypt(data);
		}
	}

	/**
	 * Derives and encapsulates a {@link javax.crypto.SecretKey}.
	 *
	 * @param rsaPublicKey          the {@link java.security.interfaces.RSAPublicKey} used for encapsulation.
	 * @param encapsulatedSecretKey a buffer to contain the encapsulated derived {@link javax.crypto.SecretKey}.
	 * @return the derived {@link javax.crypto.SecretKey}.
	 */
	private SecretKey deriveAndEncapsulateSecretKey(final RSAPublicKey rsaPublicKey, final byte[] encapsulatedSecretKey) {

		final RSAKeyParameters bcRsaPublicKey = new RSAKeyParameters(false, rsaPublicKey.getModulus(), rsaPublicKey.getPublicExponent());
		keyEncapsulation.init(bcRsaPublicKey);
		final KeyParameter bcSecretKey = (KeyParameter) keyEncapsulation.encrypt(encapsulatedSecretKey, secretKeyByteLength);

		return new SecretKeySpec(bcSecretKey.getKey(), secretKeyAlgorithm);
	}

	/**
	 * Symmetrically encrypts some data.
	 *
	 * @param secretKey the {@link javax.crypto.SecretKey} used for encryption.
	 * @param data      the data to encrypt.
	 * @return the encrypted data.
	 * @throws GeneralCryptoLibException if the encryption process fails.
	 */
	private byte[] encrypt(final SecretKey secretKey, final byte[] data) throws GeneralCryptoLibException {

		try {
			return symmetricCipher.genAuthenticatedEncryption(secretKey, data);
		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException("Asymmetric cipher with RSA-KEM could not symmetrically encrypt data with derived secret key.", e);
		}
	}

	/**
	 * Decapsulates a {@link javax.crypto.SecretKey}.
	 *
	 * @param rsaPrivateKey                         the {@link java.security.interfaces.RSAPrivateKey} used for decapsulation.
	 * @param encapsulatedSecretKeyAndEncryptedData the bitwise concatenation of the encapsulated {@link javax.crypto.SecretKey} and the encrypted
	 *                                              data.
	 * @param encapsulationByteLength               the byte length of the encapsulated {@link javax.crypto.SecretKey}.
	 * @return the decapsulated {@link javax.crypto.SecretKey}.
	 */
	private SecretKey decapsulateSecretKey(final RSAPrivateKey rsaPrivateKey, final byte[] encapsulatedSecretKeyAndEncryptedData,
			final int encapsulationByteLength) {

		final RSAKeyParameters bcRsaPrivateKey = new RSAKeyParameters(true, rsaPrivateKey.getModulus(), rsaPrivateKey.getPrivateExponent());

		final byte[] encapsulatedSecretKey = Arrays.copyOfRange(encapsulatedSecretKeyAndEncryptedData, 0, encapsulationByteLength);

		keyEncapsulation.init(bcRsaPrivateKey);
		final KeyParameter bcSecretKey = (KeyParameter) keyEncapsulation.decrypt(encapsulatedSecretKey, secretKeyByteLength);

		return new SecretKeySpec(bcSecretKey.getKey(), secretKeyAlgorithm);
	}

	/**
	 * Symmetrically decrypts some encrypted data.
	 *
	 * @param secretKey     the {@link javax.crypto.SecretKey} used for decryption.
	 * @param encryptedData the encrypted data.
	 * @return the decrypted data.
	 * @throws GeneralCryptoLibException if the decryption process fails.
	 */
	private byte[] decrypt(final SecretKey secretKey, final byte[] encryptedData) throws GeneralCryptoLibException {

		try {
			return symmetricCipher.getAuthenticatedDecryption(secretKey, encryptedData);
		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException("Asymmetric cipher with RSA-KEM could not symmetrically decrypt data with derived secret key.", e);
		}
	}

	/**
	 * Initializes the asymmetric cipher for encryption.
	 *
	 * @param publicKey the {@link java.security.PublicKey} used for encryption.
	 * @throws GeneralCryptoLibException if the initialization process fails.
	 */
	private void initCipherForEncryption(final PublicKey publicKey) throws GeneralCryptoLibException {
		try {
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not initialize asymmetric cipher for encryption.", e);
		}
	}

	/**
	 * Asymmetrically encrypts some data.
	 *
	 * @param data the data to encrypt
	 * @return the encrypted data.
	 * @throws GeneralCryptoLibException if encryption process fails.
	 */
	private byte[] encrypt(final byte[] data) throws GeneralCryptoLibException {
		try {
			return cipher.doFinal(data);
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not asymmetrically encrypt data.", e);
		}
	}

	/**
	 * Initializes the asymmetric cipher for decryption.
	 *
	 * @param privateKey the {@link java.security.PrivateKey} used for decryption.
	 * @throws GeneralCryptoLibException if the initialization process fails.
	 */
	private void initCipherForDecryption(final PrivateKey privateKey) throws GeneralCryptoLibException {
		try {
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not initialize asymmetric cipher for decryption.", e);
		}
	}

	/**
	 * Asymmetrically decrypts some encrypted data.
	 *
	 * @param encryptedData the encrypted data.
	 * @return the decrypted data.
	 * @throws GeneralCryptoLibException if the decryption process fails.
	 */
	private byte[] decrypt(final byte[] encryptedData) throws GeneralCryptoLibException {
		try {
			return cipher.doFinal(encryptedData);
		} catch (final GeneralSecurityException e) {
			throw new GeneralCryptoLibException("Could not asymmetrically decrypt data.", e);
		}
	}
}
