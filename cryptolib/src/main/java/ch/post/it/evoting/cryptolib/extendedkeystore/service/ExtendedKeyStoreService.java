/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyStore.PasswordProtection;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ExtendedKeyStoreP12PolicyFromProperties;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ExtendedKeyStorePolicy;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.ExtendedKeyStoreWithPBKDFGenerator;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.ExtendedKeyStoreWithPBKDFGeneratorFactory;

/**
 * Provides operations with stores.
 */
public class ExtendedKeyStoreService extends CryptolibService implements KeyStoreService {

	private static final String KEY_STORE_INPUT_STREAM_LABEL = "Key store input stream";

	// Ignore the Sonar rule 'PASSWORD detected in this expression, review this potentially hard-coded credential.'
	// This string represents a label, not a password.
	@SuppressWarnings("squid:S2068")
	private static final String KEY_STORE_PASSWORD_LABEL = "Key store password";
	private final ExtendedKeyStoreWithPBKDFGenerator keyStoreWithPBKDFGenerator;

	/**
	 * Default constructor which initializes all properties to default values, which are read from the properties specified by {@link
	 * ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public ExtendedKeyStoreService() {
		final ExtendedKeyStorePolicy storePolicy = new ExtendedKeyStoreP12PolicyFromProperties();

		final ExtendedKeyStoreWithPBKDFGeneratorFactory keyStoreWithPBKDFGeneratorFactory = new ExtendedKeyStoreWithPBKDFGeneratorFactory(storePolicy);
		keyStoreWithPBKDFGenerator = keyStoreWithPBKDFGeneratorFactory.create();
	}

	@Override
	public CryptoAPIExtendedKeyStore createKeyStore() {

		return keyStoreWithPBKDFGenerator.create();
	}

	@Override
	public CryptoAPIExtendedKeyStore loadKeyStore(final InputStream in, final PasswordProtection password) throws GeneralCryptoLibException {

		Validate.notNull(in, KEY_STORE_INPUT_STREAM_LABEL);
		Validate.notNull(password, "Key store password protection");
		Validate.notNullOrBlank(password.getPassword(), KEY_STORE_PASSWORD_LABEL);

		try {
			return keyStoreWithPBKDFGenerator.load(in, password.getPassword());
		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException("Could not load Extended key store.", e);
		}
	}

	@Override
	public CryptoAPIExtendedKeyStore loadKeyStore(final InputStream in, final char[] password) throws GeneralCryptoLibException {

		Validate.notNull(in, KEY_STORE_INPUT_STREAM_LABEL);
		Validate.notNullOrBlank(password, KEY_STORE_PASSWORD_LABEL);

		return loadKeyStore(in, new PasswordProtection(password));
	}

	@Override
	public CryptoAPIExtendedKeyStore loadKeyStore(final Path path, final char[] password) throws GeneralCryptoLibException, FileNotFoundException {

		Validate.notNull(path, "Key store path");
		Validate.notNullOrBlank(password, KEY_STORE_PASSWORD_LABEL);

		return loadKeyStore(new FileInputStream(path.toFile()), password);
	}

	@Override
	public CryptoAPIExtendedKeyStore loadKeyStoreFromJSON(final InputStream in, final PasswordProtection password) throws GeneralCryptoLibException {

		Validate.notNull(in, "Key store JSON input stream");
		Validate.notNull(password, "Key store password protection");
		Validate.notNullOrBlank(password.getPassword(), KEY_STORE_PASSWORD_LABEL);

		return keyStoreWithPBKDFGenerator.loadFromJSON(in, password.getPassword());
	}

	@Override
	public String formatKeyStoreToJSON(final InputStream in) throws GeneralCryptoLibException {

		Validate.notNull(in, KEY_STORE_INPUT_STREAM_LABEL);

		try {
			return keyStoreWithPBKDFGenerator.formatKeyStoreToJSON(in);
		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException("Could not format Extended key store to JSON.", e);
		}
	}
}
