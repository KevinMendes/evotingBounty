/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import static java.text.MessageFormat.format;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.primitives.derivation.factory.CryptoKeyDeriverFactory;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;

/**
 * Class that provides the functionality to create and load Derived KeyStores.
 *
 * <p>Instances of this class are immutable.
 */
public final class ExtendedKeyStoreWithPBKDFGenerator {

	private final String keyStoreType;

	private final Provider keyStoreProvider;

	private final CryptoKeyDeriverFactory cryptoKeyDeriverFactory;

	private final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory;

	private final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec;

	/**
	 * Creates an instance of the class and initialized it by the provided arguments.
	 *
	 * @param cryptoPBKDFDeriverFactory           the derivator of crypto key.
	 * @param keyStoreType                        the type of KeyStore to be created.
	 * @param keyStoreProvider                    the provider of the {@link java.security.KeyStore}.
	 * @param symmetricAuthenticatedCipherFactory the symmetric cipher factory.
	 * @param configSecretKeyAlgorithmAndSpec     the secret key type.
	 */
	ExtendedKeyStoreWithPBKDFGenerator(final CryptoKeyDeriverFactory cryptoPBKDFDeriverFactory, final String keyStoreType,
			final Provider keyStoreProvider, final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory,
			final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec) {
		cryptoKeyDeriverFactory = cryptoPBKDFDeriverFactory;
		this.keyStoreType = keyStoreType;
		this.keyStoreProvider = keyStoreProvider;
		this.symmetricAuthenticatedCipherFactory = symmetricAuthenticatedCipherFactory;
		this.configSecretKeyAlgorithmAndSpec = configSecretKeyAlgorithmAndSpec;
	}

	private static byte[] readBytes(final InputStream stream) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try {
			int length;
			final byte[] buffer = new byte[1024 * 10];
			while ((length = stream.read(buffer)) > 0) {
				bytes.write(buffer, 0, length);
			}
		} finally {
			bytes.close();
		}
		return bytes.toByteArray();
	}

	/**
	 * Creates a empty Key store, ready to add any entry.
	 *
	 * @return an empty {@link CryptoExtendedKeyStoreWithPBKDF} ready to be used.
	 */
	public CryptoExtendedKeyStoreWithPBKDF create() {
		return new CryptoExtendedKeyStoreWithPBKDF(keyStoreType, keyStoreProvider, cryptoKeyDeriverFactory.createPBKDFDeriver(),
				symmetricAuthenticatedCipherFactory, configSecretKeyAlgorithmAndSpec);
	}

	/**
	 * Loads a key store given the following parameters.
	 *
	 * @param in       the input stream from which the KeyStore is loaded.
	 * @param password the password used to open the KeyStore.
	 * @return the key store container as a {@link java.security.KeyStore}.
	 * @throws IOException               if there is an I/O or format problem with the key store data.
	 * @throws GeneralCryptoLibException if there is problems while reading store.
	 */
	public CryptoExtendedKeyStoreWithPBKDF load(final InputStream in, final char[] password) throws GeneralCryptoLibException {
		return new CryptoExtendedKeyStoreWithPBKDF(keyStoreType, keyStoreProvider, cryptoKeyDeriverFactory.createPBKDFDeriver(), in, password,
				symmetricAuthenticatedCipherFactory, configSecretKeyAlgorithmAndSpec);
	}

	/**
	 * Loads a key store given the following parameters.
	 *
	 * @param in       the input stream from which the KeyStore is loaded. Excepted in JSON format
	 * @param password the password used to open the KeyStore.
	 * @return the key store container as a {@link java.security.KeyStore}.
	 * @throws GeneralCryptoLibException if there is problems while reading store.
	 */
	public CryptoExtendedKeyStoreWithPBKDF loadFromJSON(final InputStream in, final char[] password) throws GeneralCryptoLibException {
		return new CryptoExtendedKeyStoreWithPBKDF(true, keyStoreType, keyStoreProvider, cryptoKeyDeriverFactory.createPBKDFDeriver(), in, password,
				symmetricAuthenticatedCipherFactory, configSecretKeyAlgorithmAndSpec);
	}

	/**
	 * Formats the given {@link CryptoAPIExtendedKeyStore} to JSON format.
	 *
	 * @param in the stream from the {@link CryptoAPIExtendedKeyStore} will be read.
	 * @return the given {@link CryptoAPIExtendedKeyStore} in JSON format.
	 * @throws IOException               if there is an I/O or format problem with the key store data.
	 * @throws GeneralCryptoLibException if there is problems while reading store.
	 */
	public String formatKeyStoreToJSON(final InputStream in) throws GeneralCryptoLibException {

		boolean saltInitialized = false;

		final byte[] salt = new byte[cryptoKeyDeriverFactory.getSaltBitLength() / Byte.SIZE];

		final Map<String, byte[]> secretKeys = new HashMap<>();
		final Map<String, byte[]> elGamalPrivateKeys = new HashMap<>();
		byte[] keyStoreJS = {};
		try {
			final ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in));
			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				final String[] entryName = entry.getName().split("\\.");
				final ZipEntryTypeEnum entryType = ZipEntryTypeEnum.valueOf(entryName[0].toUpperCase());
				switch (entryType) {
				case SALT:
					if (zin.read(salt) > 0) {
						saltInitialized = true;
					}
					break;
				case SECRET:
					secretKeys.put(entryName[1], readBytes(zin));
					break;
				case ELGAMAL:
					elGamalPrivateKeys.put(entryName[1], readBytes(zin));
					break;
				case STORE:
					keyStoreJS = readBytes(zin);
					break;
				default:
					throw new GeneralCryptoLibException(format("Unknown entry type ''{0}''.", entryType));
				}
				entry = zin.getNextEntry();
			}

		} catch (final IOException e) {
			throw new GeneralCryptoLibException("There was a problem reading the store." + e.getMessage(), e);
		}
		if (!saltInitialized) {
			throw new GeneralCryptoLibException("There was a problem reading the store. salt was not initialized.");
		}
		return CryptoExtendedKeyStoreWithPBKDFHelper.toJSON(salt, secretKeys, elGamalPrivateKeys, keyStoreJS);
	}
}
