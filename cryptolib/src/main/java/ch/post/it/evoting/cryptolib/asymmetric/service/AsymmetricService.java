/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.asymmetric.utils.KeyPairConverterAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.configuration.AsymmetricCipherPolicyFromProperties;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.factory.AsymmetricCipherFactory;
import ch.post.it.evoting.cryptolib.asymmetric.cipher.factory.CryptoAsymmetricCipher;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigEncryptionKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicyFromProperties;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.constants.KeyPairConstants;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.factory.CryptoKeyPairGenerator;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.factory.KeyPairGeneratorFactory;
import ch.post.it.evoting.cryptolib.asymmetric.signer.configuration.DigitalSignerPolicyFromProperties;
import ch.post.it.evoting.cryptolib.asymmetric.signer.factory.CryptoDigitalSigner;
import ch.post.it.evoting.cryptolib.asymmetric.signer.factory.DigitalSignerFactory;
import ch.post.it.evoting.cryptolib.asymmetric.utils.KeyPairConverter;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class which implements {@link AsymmetricServiceAPI}.
 *
 * <p>Instances of this class are immutable.
 */
public final class AsymmetricService extends CryptolibService implements AsymmetricServiceAPI {

	private static final String EXCEPTION = "Unrecognized asymmetric key pair algorithm:";
	private static final String PUBLIC_KEY_LABEL = "Public key";
	private static final String PUBLIC_KEY_CONTENT_LABEL = "Public key content";
	private static final String PRIVATE_KEY_LABEL = "Private key";
	private static final String PRIVATE_KEY_CONTENT_LABEL = "Private key content";
	private final CryptoKeyPairGenerator signingKeyPairGenerator;
	private final CryptoKeyPairGenerator encryptionKeyPairGenerator;
	private final CryptoAsymmetricCipher asymmetricCipher;
	private final CryptoDigitalSigner digitalSigner;
	private final String asymmetricKeyPairAlgorithm;
	private final int asymmetricKeyByteLength;

	/**
	 * Default constructor which initializes all properties to default values. These default values are obtained from the path indicated by {@link
	 * ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public AsymmetricService() {
		final KeyPairPolicy keyPairPolicy = new KeyPairPolicyFromProperties();
		signingKeyPairGenerator = new KeyPairGeneratorFactory(keyPairPolicy).createSigning();
		encryptionKeyPairGenerator = new KeyPairGeneratorFactory(keyPairPolicy).createEncryption();

		asymmetricCipher = new AsymmetricCipherFactory(new AsymmetricCipherPolicyFromProperties()).create();

		digitalSigner = new DigitalSignerFactory(new DigitalSignerPolicyFromProperties()).create();

		final ConfigEncryptionKeyPairAlgorithmAndSpec keyPairSpec = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec();
		asymmetricKeyPairAlgorithm = keyPairSpec.getAlgorithm();
		if (asymmetricKeyPairAlgorithm.equals(KeyPairConstants.RSA_ALG)) {
			asymmetricKeyByteLength = ((RSAKeyGenParameterSpec) keyPairSpec.getSpec()).getKeysize() / Byte.SIZE;
		} else {
			throw new CryptoLibException(EXCEPTION + asymmetricKeyPairAlgorithm);
		}
	}

	@Override
	public KeyPair getKeyPairForSigning() {

		return signingKeyPairGenerator.genKeyPair();
	}

	@Override
	public KeyPair getKeyPairForEncryption() {

		return encryptionKeyPairGenerator.genKeyPair();
	}

	@Override
	public byte[] encrypt(final PublicKey key, final byte[] data) throws GeneralCryptoLibException {

		Validate.notNull(key, PUBLIC_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PUBLIC_KEY_CONTENT_LABEL);
		validateKeySize(key, "encryption");
		Validate.notNullOrEmpty(data, "Data");

		return asymmetricCipher.encrypt(key, data);
	}

	@Override
	public byte[] decrypt(final PrivateKey key, final byte[] data) throws GeneralCryptoLibException {

		Validate.notNull(key, PRIVATE_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PRIVATE_KEY_CONTENT_LABEL);
		validateKeySize(key, "decryption");
		Validate.notNullOrEmpty(data, "Encrypted data");

		return asymmetricCipher.decrypt(key, data);
	}

	@Override
	public byte[] sign(final PrivateKey key, final byte[]... data) throws GeneralCryptoLibException {

		Validate.notNull(key, PRIVATE_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PRIVATE_KEY_CONTENT_LABEL);
		validateKeySize(key, "signing");
		Validate.notNullOrEmpty(data, "Data element array");
		if (data.length == 1) {
			Validate.notNullOrEmpty(data[0], "Data");
		} else {
			for (final byte[] dataElement : data) {
				Validate.notNullOrEmpty(dataElement, "A data element");
			}
		}

		return digitalSigner.sign(key, data);
	}

	@Override
	public byte[] sign(final PrivateKey key, final InputStream in) throws GeneralCryptoLibException {

		Validate.notNull(key, PRIVATE_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PRIVATE_KEY_CONTENT_LABEL);
		validateKeySize(key, "signing");
		Validate.notNull(in, "Data input stream");

		return digitalSigner.sign(key, in);
	}

	@Override
	public boolean verifySignature(final byte[] signatureBytes, final PublicKey key, final byte[]... data) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(signatureBytes, "Signature");
		Validate.notNull(key, PUBLIC_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PUBLIC_KEY_CONTENT_LABEL);
		validateKeySize(key, "signature verification");
		Validate.notNullOrEmpty(data, "Data element array");
		if (data.length == 1) {
			Validate.notNullOrEmpty(data[0], "Data");
		} else {
			for (final byte[] dataElement : data) {
				Validate.notNullOrEmpty(dataElement, "A data element");
			}
		}

		return digitalSigner.verifySignature(signatureBytes, key, data);
	}

	@Override
	public boolean verifySignature(final byte[] signatureBytes, final PublicKey key, final InputStream in) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(signatureBytes, "Signature");
		Validate.notNull(key, PUBLIC_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), PUBLIC_KEY_CONTENT_LABEL);
		validateKeySize(key, "signature verification");
		Validate.notNull(in, "Data input stream");

		return digitalSigner.verifySignature(signatureBytes, key, in);
	}

	/**
	 * Introduced as a part of an import and export of private and public keys to and from PEM Strings.
	 */
	@Override
	public KeyPairConverterAPI getKeyPairConverter() {
		return new KeyPairConverter();
	}

	private void validateKeySize(final PublicKey publicKey, final String keyType) throws GeneralCryptoLibException {

		if (asymmetricKeyPairAlgorithm.equals(KeyPairConstants.RSA_ALG)) {
			final int publicKeyLength = ((RSAPublicKey) publicKey).getModulus().bitLength() / Byte.SIZE;

			Validate.isEqual(publicKeyLength, asymmetricKeyByteLength, "Byte length of " + keyType + " public key",
					"byte length of corresponding key in cryptographic policy for asymmetric service");
		} else {
			throw new GeneralCryptoLibException(EXCEPTION + asymmetricKeyPairAlgorithm);
		}
	}

	private void validateKeySize(final PrivateKey privateKey, final String keyType) throws GeneralCryptoLibException {

		if (asymmetricKeyPairAlgorithm.equals(KeyPairConstants.RSA_ALG)) {
			final int privateKeyLength = ((RSAPrivateKey) privateKey).getModulus().bitLength() / Byte.SIZE;

			Validate.isEqual(privateKeyLength, asymmetricKeyByteLength, "Byte length of " + keyType + " private key",
					"byte length of corresponding key in cryptographic policy for asymmetric service");
		} else {
			throw new GeneralCryptoLibException(EXCEPTION + asymmetricKeyPairAlgorithm);
		}
	}
}
