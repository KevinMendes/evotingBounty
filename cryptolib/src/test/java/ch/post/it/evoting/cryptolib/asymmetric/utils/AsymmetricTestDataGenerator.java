/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Arrays;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicyFromProperties;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;

/**
 * Utility to generate various types of asymmetric data needed by tests.
 */
public class AsymmetricTestDataGenerator {

	private static final int ILLEGAL_KEY_SIZE_IN_BITS = 512;

	/**
	 * Generates an asymmetric key pair for encryption.
	 *
	 * @return the generated asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getKeyPairForEncryption() {

		final AsymmetricService asymmetricService = new AsymmetricService();

		return asymmetricService.getKeyPairForEncryption();
	}

	/**
	 * Generates an asymmetric key pair for signing.
	 *
	 * @return the generated asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getKeyPairForSigning() {

		final AsymmetricService asymmetricService = new AsymmetricService();

		return asymmetricService.getKeyPairForSigning();
	}

	/**
	 * Generates an asymmetric key pair for encryption that does not already exist in a specified collection of asymmetric key pairs.
	 *
	 * @param existingKeyPairs the collection of existing asymmetric key pairs.
	 * @return the generated unique asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getUniqueKeyPairForEncryption(final KeyPair... existingKeyPairs) throws GeneralCryptoLibException {

		KeyPair keyPair;
		do {
			keyPair = getKeyPairForEncryption();
		} while (keyPairFound(keyPair, existingKeyPairs));

		return keyPair;
	}

	/**
	 * Generates an asymmetric key pair for signing that does not already exist in a specified collection of asymmetric key pairs.
	 *
	 * @param existingKeyPairs the collection of existing asymmetric key pairs.
	 * @return the generated unique asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getUniqueKeyPairForSigning(final KeyPair... existingKeyPairs) {

		KeyPair keyPair;
		do {
			keyPair = getKeyPairForSigning();
		} while (keyPairFound(keyPair, existingKeyPairs));

		return keyPair;
	}

	/**
	 * Generates an asymmetric key pair for encryption with a key size that is considered illegal by the cryptographic policy.
	 *
	 * @return the generated asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getIllegaSizeKeyPairForEncryption() throws GeneralCryptoLibException {

		final KeyPairPolicy keyPairPolicy = new KeyPairPolicyFromProperties();

		final String algorithm = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec().getAlgorithm();

		final Provider provider = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec().getProvider();

		final KeyPairGenerator keyPairGenerator = getKeyPairGenerator(algorithm, provider, ILLEGAL_KEY_SIZE_IN_BITS);

		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Generates an asymmetric key pair for signing with a key size that is considered illegal by the cryptographic policy.
	 *
	 * @return the generated asymmetric key pair.
	 * @throws GeneralCryptoLibException if the key pair generation process fails.
	 */
	public static KeyPair getIllegalSizeKeyPairForSigning() throws GeneralCryptoLibException {

		final KeyPairPolicy keyPairPolicy = new KeyPairPolicyFromProperties();

		final String algorithm = keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm();

		final Provider provider = keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getProvider();

		final KeyPairGenerator keyPairGenerator = getKeyPairGenerator(algorithm, provider, ILLEGAL_KEY_SIZE_IN_BITS);

		return keyPairGenerator.generateKeyPair();
	}

	/**
	 * Generates a specified number of asymmetric key pairs for encryption.
	 *
	 * @param numKeyPairs the number of asymmetric key pairs to generate.
	 * @return the generated asymmetric key pairs.
	 * @throws GeneralCryptoLibException if any key pair generation process fails.
	 */
	public static KeyPair[] getKeyPairsForEncryption(final int numKeyPairs) throws GeneralCryptoLibException {

		final KeyPair[] keyPairs = new KeyPair[numKeyPairs];
		for (int i = 0; i < numKeyPairs; i++) {
			keyPairs[i] = getKeyPairForEncryption();
		}

		return keyPairs;
	}

	/**
	 * Generates a specified number of asymmetric key pairs for signing.
	 *
	 * @param numKeyPairs the number of asymmetric key pairs to generate.
	 * @return the generated asymmetric key pairs.
	 * @throws GeneralCryptoLibException if any key pair generation process fails.
	 */
	public static KeyPair[] getKeyPairsForSigning(final int numKeyPairs) throws GeneralCryptoLibException {

		final KeyPair[] keyPairs = new KeyPair[numKeyPairs];
		for (int i = 0; i < numKeyPairs; i++) {
			keyPairs[i] = getKeyPairForSigning();
		}

		return keyPairs;
	}

	/**
	 * Retrieves the public keys from a collection of one or more asymmetric key pairs.
	 *
	 * @param keyPairs the collection of asymmetric key pairs.
	 * @return the collection of retrieved asymmetric public keys.
	 */
	public static PublicKey[] extractPublicKeys(final KeyPair... keyPairs) {

		final int numPublicKeys = keyPairs.length;
		final PublicKey[] publicKeys = new PublicKey[numPublicKeys];
		for (int i = 0; i < numPublicKeys; i++) {
			publicKeys[i] = keyPairs[i].getPublic();
		}

		return publicKeys;
	}

	/**
	 * Retrieves the private keys from a collection of one or more asymmetric key pairs.
	 *
	 * @param keyPairs the collection of asymmetric key pairs.
	 * @return the collection of retrieved asymmetric private keys.
	 */
	public static PrivateKey[] extractPrivateKeys(final KeyPair... keyPairs) {

		final int numPrivateKeys = keyPairs.length;
		final PrivateKey[] privateKeys = new PrivateKey[numPrivateKeys];
		for (int i = 0; i < numPrivateKeys; i++) {
			privateKeys[i] = keyPairs[i].getPrivate();
		}

		return privateKeys;
	}

	private static boolean keyPairFound(final KeyPair keyPair, final KeyPair[] existingList) {

		for (final KeyPair pair : existingList) {
			if (Arrays.equals(pair.getPrivate().getEncoded(), keyPair.getPrivate().getEncoded())) {
				return true;
			}
		}

		return false;
	}

	private static KeyPairGenerator getKeyPairGenerator(final String algorithm, final Provider provider, final int keySize)
			throws GeneralCryptoLibException {

		final KeyPairGenerator keyPairGenerator;
		try {
			if (Provider.DEFAULT == provider) {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			} else {
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm, provider.getProviderName());
			}
		} catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new GeneralCryptoLibException("Could not create instance of KeyPairGenerator.", e);
		}

		final AlgorithmParameterSpec spec = new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4);

		try {
			keyPairGenerator.initialize(spec);
		} catch (final InvalidAlgorithmParameterException e) {
			throw new GeneralCryptoLibException("Could not initialize instance of KeyPairGenerator.", e);
		}

		return keyPairGenerator;
	}
}
