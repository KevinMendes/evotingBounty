/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import ch.post.it.evoting.cryptolib.CryptolibService;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeyStorePolicy;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeyStorePolicyFromProperties;

/**
 * Class which implements {@link StoresServiceAPI}.
 *
 * <p>Instances of this class are immutable.
 */
public class StoresService extends CryptolibService implements StoresServiceAPI {

	private final Provider provider;

	/**
	 * Default constructor, which retrieves the stores cryptographic policy from the default properties file {@link
	 * ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper#CRYPTOLIB_POLICY_PROPERTIES_FILE_PATH}.
	 */
	public StoresService() {
		final KeyStorePolicy storePolicy = new KeyStorePolicyFromProperties();
		provider = storePolicy.getKeyStoreSpec().getProvider();
	}

	@Override
	public KeyStore createKeyStore(final KeyStoreType type) throws GeneralCryptoLibException {

		Validate.notNull(type, "Key store type");

		try {
			final KeyStore keyStore = getKeyStoreInstance(type);

			keyStore.load(null, null);

			return keyStore;
		} catch (final NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new GeneralCryptoLibException("Could not create key store.", e);
		}
	}

	@Override
	public KeyStore loadKeyStore(final KeyStoreType type, final InputStream inStream, final char[] password) throws GeneralCryptoLibException {

		Validate.notNull(type, "Key store type");
		Validate.notNull(inStream, "Key store input stream");
		Validate.notNullOrBlank(password, "Key store password");

		try {
			final KeyStore keyStore = getKeyStoreInstance(type);

			keyStore.load(inStream, password);

			return keyStore;
		} catch (final NoSuchAlgorithmException | CertificateException | IOException e) {
			throw new GeneralCryptoLibException("Could not load key store.", e);
		}
	}

	private KeyStore getKeyStoreInstance(final KeyStoreType type) throws GeneralCryptoLibException {

		try {
			if (provider == Provider.DEFAULT) {
				return KeyStore.getInstance(type.getKeyStoreTypeName());
			} else {
				final String providerName = getProviderName(type);

				return KeyStore.getInstance(type.getKeyStoreTypeName(), providerName);
			}
		} catch (final KeyStoreException | NoSuchProviderException e) {
			throw new GeneralCryptoLibException("Could not get key store instance.", e);
		}
	}

	private String getProviderName(final KeyStoreType type) {

		// Note: Supported SUN cryptographic service provider will depend on
		// type of key store being used.
		String providerName = provider.getProviderName();
		if (provider == Provider.SUN) {
			if (type == KeyStoreType.PKCS12) {
				providerName = Provider.SUN_JSSE.getProviderName();
			} else {
				providerName = Provider.SUN_JCE.getProviderName();
			}
		}

		return providerName;
	}
}
