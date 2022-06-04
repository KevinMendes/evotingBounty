/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import java.io.InputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipher;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.key.factory.CryptoSecretKeyGeneratorForEncryption;
import ch.post.it.evoting.cryptolib.symmetric.key.factory.CryptoSecretKeyGeneratorForHmac;
import ch.post.it.evoting.cryptolib.symmetric.key.factory.SecretKeyGeneratorFactory;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicy;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.mac.factory.CryptoMac;
import ch.post.it.evoting.cryptolib.symmetric.mac.factory.MacFactory;

/**
 * Class which offers symmetric services.
 *
 * <p>Note: repeated calls to the methods in this class will return the same object instances.
 *
 * <p>Instances of this class are immutable.
 */
public final class SymmetricService extends CryptolibService implements SymmetricServiceAPI {

	private static final String SECRET_KEY_LABEL = "Secret key";
	private static final String SECRET_KEY_CONTENT_LABEL = "Secret key content";
	private final CryptoSecretKeyGeneratorForEncryption secretKeyGeneratorForEncryption;
	private final CryptoSecretKeyGeneratorForHmac secretKeyGeneratorForHmac;
	private final SymmetricAuthenticatedCipher symmetricAuthenticatedCipher;
	private final CryptoMac cryptoMac;
	private final SymmetricKeyPolicy symmetricKeyPolicy;

	/**
	 * Initializes all properties to default values. These default values can be overridden by adding a cryptolibPolicy.properties file to the path
	 * defined in {@link ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public SymmetricService() {
		symmetricKeyPolicy = new SymmetricKeyPolicyFromProperties();
		final SecretKeyGeneratorFactory secretKeyGeneratorFactory = new SecretKeyGeneratorFactory(symmetricKeyPolicy);
		secretKeyGeneratorForEncryption = secretKeyGeneratorFactory.createGeneratorForEncryption();
		secretKeyGeneratorForHmac = secretKeyGeneratorFactory.createGeneratorForHmac();

		final SymmetricCipherPolicy symmetricCipherPolicy = new SymmetricCipherPolicyFromProperties();
		final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory = new SymmetricAuthenticatedCipherFactory(symmetricCipherPolicy);
		symmetricAuthenticatedCipher = symmetricAuthenticatedCipherFactory.create();

		final MacPolicy macPolicy = new MacPolicyFromProperties();
		final MacFactory macFactory = new MacFactory(macPolicy);
		cryptoMac = macFactory.create();
	}

	@Override
	public SecretKey getSecretKeyForEncryption() {
		return secretKeyGeneratorForEncryption.genSecretKey();
	}

	@Override
	public SecretKey getSecretKeyForHmac() {
		return secretKeyGeneratorForHmac.genSecretKey();
	}

	@Override
	public byte[] encrypt(final SecretKey key, final byte[] data) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNullOrEmpty(data, "Data");
		Validate.isEqual(key.getEncoded().length * Byte.SIZE, symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec().getKeyLength(),
				"The specified key's length", "symmetric key length in the policy");
		return symmetricAuthenticatedCipher.genAuthenticatedEncryption(key, data);
	}

	@Override
	public byte[] decrypt(final SecretKey key, final byte[] data) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNullOrEmpty(data, "Encrypted data");

		return symmetricAuthenticatedCipher.getAuthenticatedDecryption(key, data);
	}

	@Override
	public byte[] getMac(final SecretKey key, final byte[]... data) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNullOrEmpty(data, "Data element array");
		if (data.length == 1) {
			Validate.notNullOrEmpty(data[0], "Data");
		} else {
			for (final byte[] dataElement : data) {
				Validate.notNullOrEmpty(dataElement, "A data element");
			}
		}

		return cryptoMac.generate(key, data);
	}

	@Override
	public byte[] getMac(final SecretKey key, final InputStream in) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNull(in, "Data input stream");

		return cryptoMac.generate(key, in);
	}

	@Override
	public boolean verifyMac(final SecretKey key, final byte[] mac, final byte[]... data) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNullOrEmpty(mac, "MAC");
		Validate.notNullOrEmpty(data, "Data element array");
		if (data.length == 1) {
			Validate.notNullOrEmpty(data[0], "Data");
		} else {
			for (final byte[] dataElement : data) {
				Validate.notNullOrEmpty(dataElement, "A data element");
			}
		}

		return cryptoMac.verify(key, mac, data);
	}

	@Override
	public boolean verifyMac(final SecretKey key, final byte[] mac, final InputStream in) throws GeneralCryptoLibException {

		Validate.notNull(key, SECRET_KEY_LABEL);
		Validate.notNullOrEmpty(key.getEncoded(), SECRET_KEY_CONTENT_LABEL);
		Validate.notNullOrEmpty(mac, "MAC");
		Validate.notNull(in, "Data input stream");

		return cryptoMac.verify(key, mac, in);
	}

	@Override
	public SecretKey getSecretKeyForMacFromDerivedKey(final CryptoAPIDerivedKey key) throws GeneralCryptoLibException {

		Validate.notNull(key, "Derived key");
		Validate.notNullOrEmpty(key.getEncoded(), "Derived key content");

		return getSecretKeyForMacFromBytes(key.getEncoded());
	}

	@Override
	public SecretKey getSecretKeyForEncryptionFromDerivedKey(final CryptoAPIDerivedKey key) throws GeneralCryptoLibException {

		Validate.notNull(key, "Derived key");
		Validate.notNullOrEmpty(key.getEncoded(), "Derived key content");

		return getSecretKeyForEncryptionFromBytes(key.getEncoded());
	}

	@Override
	public SecretKey getSecretKeyForEncryptionFromBytes(final byte[] keyBytes) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(keyBytes, SECRET_KEY_LABEL);
		Validate.isEqual(keyBytes.length * Byte.SIZE, symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec().getKeyLength(), "The specified key's length",
				"symmetric key length in the policy");

		final String algorithm = symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec().getAlgorithm();

		return new SecretKeySpec(keyBytes, algorithm);
	}

	private SecretKey getSecretKeyForMacFromBytes(final byte[] keyBytes) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(keyBytes, SECRET_KEY_LABEL);

		final String algorithm = symmetricKeyPolicy.getHmacSecretKeyAlgorithmAndSpec().getAlgorithm();

		return new SecretKeySpec(keyBytes, algorithm);
	}
}
