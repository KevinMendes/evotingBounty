/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import static java.text.MessageFormat.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.binary.ByteArrays;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.constants.ExtendedKeyStoreConstants;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.primitives.derivation.factory.CryptoPBKDFDeriver;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipher;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;

/**
 * Class which provides functionality to perform operations with a key store.
 *
 * <p>The {@link CryptoExtendedKeyStoreWithPBKDF} given is used in derivation process. New entries can
 * be added to store and already existent entries can be updated if it is used a valid password.
 */
public final class CryptoExtendedKeyStoreWithPBKDF implements CryptoAPIExtendedKeyStore {

	private static final String ADD_ENTRY_ERROR = "Could not add the entry.";

	private static final String CREATE_ERROR = "Could not create the key store.";

	private static final String LOAD_ERROR = "Could not load key store.";

	private static final String ELGAMAL_PRIVATE_KEY_ALIAS = "ElGamal private key alias";

	private static final String PRIVATE_KEY_ALIAS = "Private key alias";

	private static final String SECRET_KEY_ALIAS = "Secret key alias";

	private final DerivedKeyCache cache = new DerivedKeyCacheImpl();

	private final CryptoPBKDFDeriver cryptoPBKDFDeriver;

	private final KeyStore keyStore;

	private final Provider keyStoreProvider;

	private final String keyStoreType;

	private final byte[] salt;

	private final List<String> secretKeyAliases = new ArrayList<>();

	private final List<String> privateKeyAliases = new ArrayList<>();

	private final Set<String> elGamalPrivateKeyAliases = new LinkedHashSet<>();

	private final Map<String, byte[]> secretKeys = new HashMap<>();

	private final Map<String, byte[]> elGamalPrivateKeys = new HashMap<>();

	private final SymmetricAuthenticatedCipher cryptoSymmetricAuthenticatedCipher;

	private final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec;

	/**
	 * Creates an instance of the class and initializes it by provided parameters.
	 *
	 * @param keyStoreType                        the type of KeyStore to be created.
	 * @param keyStoreProvider                    the provider of the {@link java.security.KeyStore}.
	 * @param cryptoPBKDFDeriver                  the password deriver.
	 * @param symmetricAuthenticatedCipherFactory the symmetric cipher factory.
	 * @param configSecretKeyAlgorithmAndSpec     the secret key type.
	 */
	CryptoExtendedKeyStoreWithPBKDF(final String keyStoreType, final Provider keyStoreProvider, final CryptoPBKDFDeriver cryptoPBKDFDeriver,
			final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory,
			final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec) {

		this.keyStoreType = keyStoreType;
		this.keyStoreProvider = keyStoreProvider;
		this.cryptoPBKDFDeriver = cryptoPBKDFDeriver;

		salt = this.cryptoPBKDFDeriver.generateRandomSalt();
		keyStore = createEmptyKeyStore();

		this.configSecretKeyAlgorithmAndSpec = configSecretKeyAlgorithmAndSpec;
		cryptoSymmetricAuthenticatedCipher = symmetricAuthenticatedCipherFactory.create();
	}

	/**
	 * Creates an instance of the class and initializes it by provided parameters.
	 *
	 * @param keyStoreType                        the type of KeyStore to be created.
	 * @param keyStoreProvider                    the provider of the {@link java.security.KeyStore}.
	 * @param cryptoPBKDFDeriver                  the password deriver.
	 * @param in                                  the stream to read the store from.
	 * @param password                            the password.
	 * @param symmetricAuthenticatedCipherFactory the symmetric cipher factory.
	 * @param configSecretKeyAlgorithmAndSpec     the secret key type.
	 * @throws GeneralCryptoLibException if there is problems while reading store.
	 */
	CryptoExtendedKeyStoreWithPBKDF(final String keyStoreType, final Provider keyStoreProvider, final CryptoPBKDFDeriver cryptoPBKDFDeriver,
			final InputStream in, final char[] password, final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory,
			final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec) throws GeneralCryptoLibException {

		boolean saltInitialized = false;

		this.keyStoreType = keyStoreType;
		this.keyStoreProvider = keyStoreProvider;
		this.cryptoPBKDFDeriver = cryptoPBKDFDeriver;
		salt = new byte[this.cryptoPBKDFDeriver.getSaltBytesLength()];

		this.configSecretKeyAlgorithmAndSpec = configSecretKeyAlgorithmAndSpec;
		cryptoSymmetricAuthenticatedCipher = symmetricAuthenticatedCipherFactory.create();

		byte[] storeAsBytes = {};
		try {
			final ZipInputStream zin = new ZipInputStream(new BufferedInputStream(in));
			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				final String[] entryName = entry.getName().split("\\.");
				final ZipEntryTypeEnum zipEntryType = ZipEntryTypeEnum.valueOf(entryName[0].toUpperCase());
				switch (zipEntryType) {
				case SALT:
					if (zin.read(salt) > 0) {
						saltInitialized = true;
					}
					break;
				case SECRET:
					secretKeys.put(entryName[1], readBytes(zin));
					secretKeyAliases.add(entryName[1]);
					break;
				case ELGAMAL:
					elGamalPrivateKeys.put(entryName[1], readBytes(zin));
					elGamalPrivateKeyAliases.add(entryName[1]);
					break;
				case STORE:
					storeAsBytes = readBytes(zin);
					break;
				default:
					throw new GeneralCryptoLibException(format("Unknown entry type ''{0}''.", zipEntryType));
				}
				entry = zin.getNextEntry();
			}

		} catch (final IOException e) {
			throw new GeneralCryptoLibException("There was a problem reading the store. " + e.getMessage(), e);
		}
		if (!saltInitialized) {
			throw new GeneralCryptoLibException("There was a problem reading the store. Salt was not initialized.");
		}

		keyStore = loadKeyStore(storeAsBytes, password);

		try {
			String keyAlias;

			for (final Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {

				keyAlias = aliases.nextElement();
				if (keyStore.entryInstanceOf(keyAlias, KeyStore.SecretKeyEntry.class)) {
					secretKeyAliases.add(keyAlias);
				} else {
					privateKeyAliases.add(keyAlias);
				}
			}
		} catch (final KeyStoreException e) {
			throw new CryptoLibException(e);
		}
	}

	/**
	 * Creates an instance of the class and initializes it by provided parameters.
	 *
	 * @param fromJson                            indicates that store is in JSON.
	 * @param keyStoreType                        the type of KeyStore to be created.
	 * @param keyStoreProvider                    the provider of the {@link java.security.KeyStore}.
	 * @param cryptoPBKDFDeriver                  the password deriver.
	 * @param in                                  the stream to read the store from.
	 * @param password                            the password.
	 * @param symmetricAuthenticatedCipherFactory the symmetric cipher factory.
	 * @param configSecretKeyAlgorithmAndSpec     the secret key type.
	 * @throws GeneralCryptoLibException if there is problems while reading store.
	 */
	public CryptoExtendedKeyStoreWithPBKDF(final boolean fromJson, final String keyStoreType, final Provider keyStoreProvider,
			final CryptoPBKDFDeriver cryptoPBKDFDeriver, final InputStream in, final char[] password,
			final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory,
			final ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec) throws GeneralCryptoLibException {

		this.keyStoreType = keyStoreType;
		this.keyStoreProvider = keyStoreProvider;
		this.cryptoPBKDFDeriver = cryptoPBKDFDeriver;
		salt = new byte[this.cryptoPBKDFDeriver.getSaltBytesLength()];

		this.configSecretKeyAlgorithmAndSpec = configSecretKeyAlgorithmAndSpec;
		cryptoSymmetricAuthenticatedCipher = symmetricAuthenticatedCipherFactory.create();

		final ObjectMapper mapper = new ObjectMapper();
		try {
			final HashMap<?, ?> jsonTree = mapper.readValue(in, HashMap.class);
			final String saltEncoded = (String) jsonTree.get("salt");
			System.arraycopy(Base64.getDecoder().decode(saltEncoded), 0, salt, 0, this.cryptoPBKDFDeriver.getSaltBytesLength());

			final HashMap<?, ?> secrets = (HashMap<?, ?>) jsonTree.get("secrets");
			if (secrets != null) {
				for (final Map.Entry<?, ?> entry : secrets.entrySet()) {
					final String alias = entry.getKey().toString();
					final byte[] key = Base64.getDecoder().decode(entry.getValue().toString());
					secretKeys.put(alias, key);
					secretKeyAliases.add(alias);
				}
			}

			final HashMap<?, ?> elGamalPrivateKeysFromJsonTree = (HashMap<?, ?>) jsonTree.get("egPrivKeys");
			if (elGamalPrivateKeysFromJsonTree != null) {
				for (final Map.Entry<?, ?> entry : elGamalPrivateKeysFromJsonTree.entrySet()) {
					final String alias = entry.getKey().toString();
					final byte[] key = Base64.getDecoder().decode(entry.getValue().toString());
					this.elGamalPrivateKeys.put(alias, key);
					elGamalPrivateKeyAliases.add(alias);
				}
			}

			final String storeEncoded = (String) jsonTree.get("store");
			final byte[] storeAsByte = Base64.getDecoder().decode(storeEncoded);

			if (storeAsByte == null) {
				keyStore = createEmptyKeyStore();
			} else {
				keyStore = loadKeyStore(storeAsByte, password);

				try {
					String keyAlias;

					for (final Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {

						keyAlias = aliases.nextElement();
						if (keyStore.entryInstanceOf(keyAlias, KeyStore.SecretKeyEntry.class)) {
							secretKeyAliases.add(keyAlias);
						} else {
							privateKeyAliases.add(keyAlias);
						}
					}
				} catch (final KeyStoreException e) {
					throw new CryptoLibException(e);
				}
			}

		} catch (final IOException e) {
			throw new CryptoLibException(e);
		}
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

	private static KeyStore getInstance(final String keyStoreType, final Provider provider) throws GeneralSecurityException {
		if (provider == Provider.DEFAULT) {
			return KeyStore.getInstance(keyStoreType);
		} else {
			return KeyStore.getInstance(keyStoreType, provider.getProviderName());
		}
	}

	private static char[] derivePassword(final byte[] key) {
		return new BigInteger(key).toString(Character.MAX_RADIX).toCharArray();
	}

	private static void checkDecryptedAlias(final String alias, final byte[] decrypt) throws GeneralCryptoLibException {
		final byte[] decryptedAlias = Arrays.copyOf(decrypt, alias.length());
		final byte[] aliasBytes = alias.getBytes(StandardCharsets.UTF_8);
		if (!Arrays.equals(decryptedAlias, aliasBytes)) {
			throw new GeneralCryptoLibException("Data was tampered");
		}
	}

	private static void validatePrivateKeyEntry(final String alias, final PrivateKey key, final char[] password, final Certificate[] chain)
			throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, PRIVATE_KEY_ALIAS);
		Validate.inRange(alias.length(), ExtendedKeyStoreConstants.MINIMUM_SKS_ENTRY_ALIAS_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH, "Private key alias length", "", "");
		Validate.onlyContains(alias, Pattern.compile(ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS), PRIVATE_KEY_ALIAS);
		Validate.notNull(key, "Private key");
		Validate.notNullOrEmpty(key.getEncoded(), "Private key content");
		Validate.notNullOrBlank(password, "Private key password");
		Validate.inRange(password.length, ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH, "Private key password length", "", "");
		Validate.notNullOrEmpty(chain, "Certificate chain");
		for (final Certificate certificate : chain) {
			Validate.notNull(certificate, "Certificate in chain");
			try {
				Validate.notNullOrEmpty(certificate.getEncoded(), "Content of certificate in chain");
			} catch (final CertificateEncodingException e) {
				throw new GeneralCryptoLibException("Could not validate content of certificate in chain.", e);
			}
		}
	}

	private static void validateSecretKeyEntry(final String alias, final SecretKey secretKey, final char[] password)
			throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, SECRET_KEY_ALIAS);
		Validate.inRange(alias.length(), ExtendedKeyStoreConstants.MINIMUM_SKS_ENTRY_ALIAS_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH, "Secret key alias length", "", "");
		Validate.onlyContains(alias, Pattern.compile(ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS), SECRET_KEY_ALIAS);
		Validate.notNull(secretKey, "Secret key");
		Validate.notNullOrEmpty(secretKey.getEncoded(), "Secret key content");
		Validate.notNullOrBlank(password, "Secret key password");
		Validate.inRange(password.length, ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH, "Secret key password length", "", "");
	}

	private static void validateElGamalKeyEntry(final String alias, final ElGamalPrivateKey key, final char[] password)
			throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, ELGAMAL_PRIVATE_KEY_ALIAS);
		Validate.inRange(alias.length(), ExtendedKeyStoreConstants.MINIMUM_SKS_ENTRY_ALIAS_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH, "ElGamal private key alias length", "", "");
		Validate.onlyContains(alias, Pattern.compile(ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS), ELGAMAL_PRIVATE_KEY_ALIAS);
		Validate.notNull(key, "ElGamal private key");
		Validate.notNullOrBlank(password, "ElGamal private key password");
		Validate.inRange(password.length, ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH, "ElGamal private key password length", "", "");
	}

	private KeyStore createEmptyKeyStore() {
		final KeyStore store;
		try {
			store = getInstance(keyStoreType, keyStoreProvider);
			store.load(null, null);
			return store;

		} catch (final GeneralSecurityException | IOException e) {
			throw new CryptoLibException(CREATE_ERROR, e);
		}
	}

	private byte[] deriveKey(final char[] password) throws GeneralCryptoLibException {
		byte[] key = cache.get(password);
		if (key == null) {
			key = cryptoPBKDFDeriver.deriveKey(password, salt).getEncoded();
		}
		return key;
	}

	@Override
	public Certificate[] getCertificateChain(final String alias) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, "Certificate chain alias");

		try {
			return keyStore.getCertificateChain(alias);
		} catch (final KeyStoreException e) {
			throw new CryptoLibException(e);
		}
	}

	/**
	 * Returns the {@link SecretKey} associated with the given alias, using the given password to recover it.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param password the password used to seal the {@link SecretKey} in the store. The password must contain a minimum of 16 characters and a
	 *                 maximum of 1000 characters.
	 * @return the Secret Key.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#getKey(String, char[])
	 */
	@Override
	public SecretKey getSecretKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, SECRET_KEY_ALIAS);
		Validate.notNullOrBlank(password, "Secret key password");

		final byte[] data = secretKeys.get(alias);
		if (data == null || data.length == 0) {
			return null;
		}

		final String algorithm = configSecretKeyAlgorithmAndSpec.getAlgorithm();

		final byte[] key = deriveKey(password);
		final SecretKey secretKey = new SecretKeySpec(key, algorithm);

		final byte[] decrypt = cryptoSymmetricAuthenticatedCipher.getAuthenticatedDecryption(secretKey, data);

		checkDecryptedLength(alias, decrypt);
		checkDecryptedAlias(alias, decrypt);

		cache.putForSecretKey(alias, password, key);

		return new SecretKeySpec(decrypt, alias.length(), decrypt.length - alias.length(), algorithm);
	}

	private void checkDecryptedLength(final String alias, final byte[] decrypt) throws GeneralCryptoLibException {
		if (decrypt.length != configSecretKeyAlgorithmAndSpec.getKeyLength() / Byte.SIZE + alias.length()) {
			throw new GeneralCryptoLibException("Data was tampered");
		}
	}

	/**
	 * Returns the {@link PrivateKey} associated with the given alias, using the given password to recover it.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param password the password used to seal the key in the store. The password must contain a minimum of 16 characters and a maximum of 1000
	 *                 characters.
	 * @return the {@link SecretKey}.
	 * @throws GeneralCryptoLibException if the retrieving of secret key failed.
	 * @see java.security.KeyStore#getKey(String, char[])
	 */
	public PrivateKey getPrivateKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, PRIVATE_KEY_ALIAS);
		Validate.notNullOrBlank(password, "Private key password");

		final byte[] key = deriveKey(password);
		final char[] derivedPassword = derivePassword(key);
		final PrivateKey privateKey;
		try {
			privateKey = (PrivateKey) keyStore.getKey(alias, derivedPassword);
		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException("Could not retrieve private key from key store, using derived password", e);
		}
		cache.putForPrivateKey(alias, password, key);
		return privateKey;
	}

	@Override
	public List<String> getSecretKeyAliases() {
		return new ArrayList<>(secretKeyAliases);
	}

	@Override
	public List<String> getPrivateKeyAliases() {
		return new ArrayList<>(privateKeyAliases);
	}

	@Override
	public List<String> getElGamalPrivateKeyAliases() {
		return new ArrayList<>(elGamalPrivateKeyAliases);
	}

	private KeyStore loadKeyStore(final byte[] bytes, final char[] password) throws GeneralCryptoLibException {
		final byte[] key = deriveKey(password);
		final char[] derivedPassword = derivePassword(key);
		final KeyStore store;
		try (final InputStream stream = new ByteArrayInputStream(bytes)) {
			store = getInstance(keyStoreType, keyStoreProvider);
			store.load(stream, derivedPassword);
		} catch (final GeneralSecurityException | IOException e) {
			throw new GeneralCryptoLibException(LOAD_ERROR, e);
		}
		cache.putForKeyStore(password, key);
		return store;
	}

	/**
	 * Assigns the given {@link PrivateKey} and the given {@link Certificate} chain to the given alias, protecting it with the given password.
	 *
	 * @param alias    the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param key      the {@link PrivateKey} to be associated with the alias.
	 * @param password the password used to seal the private key and the chain in the store. The password must contain a minimum of 16 characters and
	 *                 a maximum of 1000 characters.
	 * @param chain    the {@link Certificate} chain for the corresponding {@link PrivateKey}. The chain should contain the leaf at the first position
	 *                 of the array.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#setKeyEntry(String, java.security.Key, char[], Certificate[])
	 */
	@Override
	public void setPrivateKeyEntry(final String alias, final PrivateKey key, final char[] password, final Certificate[] chain)
			throws GeneralCryptoLibException {

		validatePrivateKeyEntry(alias, key, password, chain);

		final byte[] derivedKey = deriveKey(password);
		final char[] derivedPassword = derivePassword(derivedKey);
		try {
			keyStore.setKeyEntry(alias, key, derivedPassword, chain);
		} catch (final KeyStoreException e) {
			throw new CryptoLibException(ADD_ENTRY_ERROR, e);
		}
		privateKeyAliases.add(alias);
		cache.putForPrivateKey(alias, password, derivedKey);
	}

	/**
	 * Assigns the given {@link SecretKey} to the given alias, protecting it with the given password.
	 *
	 * @param alias     the alias, a text of minimum 1 char, maximum 50 chars, from the alphabet [a-z0-9_-].
	 * @param secretKey the {@link SecretKey} to be associated with the alias.
	 * @param password  the password used to seal the {@link SecretKey} in the store. The password must contain a minimum of 16 characters and a
	 *                  maximum of 1000 characters.
	 * @throws GeneralCryptoLibException if arguments are invalid.
	 * @see java.security.KeyStore#setKeyEntry(String, Key, char[], Certificate[])
	 */
	@Override
	public void setSecretKeyEntry(final String alias, final SecretKey secretKey, final char[] password) throws GeneralCryptoLibException {

		validateSecretKeyEntry(alias, secretKey, password);

		final byte[] encodedKey = secretKey.getEncoded();
		final byte[] data = ByteArrays.concatenate(alias.getBytes(StandardCharsets.UTF_8), encodedKey);

		final byte[] derivedKey = deriveKey(password);
		final SecretKeySpec secretKeySpec = new SecretKeySpec(derivedKey, configSecretKeyAlgorithmAndSpec.getAlgorithm());

		final byte[] encrypted = cryptoSymmetricAuthenticatedCipher.genAuthenticatedEncryption(secretKeySpec, data);
		secretKeys.put(alias, encrypted);
		secretKeyAliases.add(alias);
		cache.putForSecretKey(alias, password, derivedKey);
	}

	@Override
	public void setElGamalPrivateKeyEntry(final String alias, final ElGamalPrivateKey key, final char[] password) throws GeneralCryptoLibException {

		validateElGamalKeyEntry(alias, key, password);

		final byte[] encodedKey = key.toJson().getBytes(StandardCharsets.UTF_8);
		final byte[] data = ByteArrays.concatenate(alias.getBytes(StandardCharsets.UTF_8), encodedKey);

		final byte[] derivedKey = deriveKey(password);
		final SecretKeySpec secretKeySpec = new SecretKeySpec(derivedKey, configSecretKeyAlgorithmAndSpec.getAlgorithm());

		final byte[] encrypted = cryptoSymmetricAuthenticatedCipher.genAuthenticatedEncryption(secretKeySpec, data);
		elGamalPrivateKeys.put(alias, encrypted);
		elGamalPrivateKeyAliases.add(alias);
		cache.putForElGamalPrivateKey(alias, password, derivedKey);
	}

	@Override
	public ElGamalPrivateKey getElGamalPrivateKeyEntry(final String alias, final char[] password) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(alias, ELGAMAL_PRIVATE_KEY_ALIAS);
		Validate.notNullOrBlank(password, "ElGamal private key password");

		final byte[] data = elGamalPrivateKeys.get(alias);
		if (data == null || data.length == 0) {
			return null;
		}

		final byte[] key = deriveKey(password);
		final SecretKey secretKey = new SecretKeySpec(key, configSecretKeyAlgorithmAndSpec.getAlgorithm());

		final byte[] decrypt = cryptoSymmetricAuthenticatedCipher.getAuthenticatedDecryption(secretKey, data);
		checkDecryptedAlias(alias, decrypt);

		cache.putForElGamalPrivateKey(alias, password, key);

		final int length = decrypt.length - alias.length();
		final String json = new String(decrypt, alias.length(), length, StandardCharsets.UTF_8);
		return ElGamalPrivateKey.fromJson(json);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param out      {@inheritDoc}
	 * @param password {@inheritDoc} A password of a minimum of {@value ExtendedKeyStoreConstants#MINIMUM_SKS_PASSWORD_LENGTH } characters and a
	 *                 maximum of {@value ExtendedKeyStoreConstants#MAXIMUM_SKS_PASSWORD_LENGTH}.
	 * @throws GeneralCryptoLibException
	 */
	@Override
	public void store(final OutputStream out, final char[] password) throws GeneralCryptoLibException {

		Validate.notNull(out, "Key store output stream");
		Validate.notNullOrBlank(password, "Key store password");
		Validate.inRange(password.length, ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH,
				ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH, "Key store password length", "", "");

		final byte[] derivedKey = deriveKey(password);
		final char[] derivePassword = derivePassword(derivedKey);

		final ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(out));
		try {

			final ZipEntry saltEntry = new ZipEntry(ZipEntryTypeEnum.SALT.name());
			zos.putNextEntry(saltEntry);
			zos.write(salt);

			for (final Map.Entry<String, byte[]> entry : secretKeys.entrySet()) {
				final ZipEntry secretEntry = new ZipEntry(String.format("%s.%s", ZipEntryTypeEnum.SECRET.name(), entry.getKey()));
				zos.putNextEntry(secretEntry);
				zos.write(entry.getValue());
			}

			for (final Map.Entry<String, byte[]> entry : elGamalPrivateKeys.entrySet()) {
				final ZipEntry keyEntry = new ZipEntry(String.format("%s.%s", ZipEntryTypeEnum.ELGAMAL.name(), entry.getKey()));
				zos.putNextEntry(keyEntry);
				zos.write(entry.getValue());
			}

			final ZipEntry storeEntry = new ZipEntry(ZipEntryTypeEnum.STORE.name());
			zos.putNextEntry(storeEntry);
			try {
				keyStore.store(zos, derivePassword);
			} catch (final GeneralSecurityException e) {
				throw new CryptoLibException(e);
			}

			zos.finish();
			zos.flush();

		} catch (final IOException e) {
			throw new CryptoLibException(CREATE_ERROR, e);
		}
		cache.putForKeyStore(password, derivedKey);
	}

	@Override
	public String toJSON(final char[] password) throws GeneralCryptoLibException {
		final byte[] key = deriveKey(password);
		final char[] derivedPassword = derivePassword(key);
		final byte[] keyStoreJS;
		try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
			keyStore.store(stream, derivedPassword);
			keyStoreJS = stream.toByteArray();
		} catch (final GeneralSecurityException | IOException e) {
			throw new GeneralCryptoLibException("Failed to serialize to JSON.", e);
		}
		cache.putForKeyStore(password, key);
		return CryptoExtendedKeyStoreWithPBKDFHelper.toJSON(salt, secretKeys, elGamalPrivateKeys, keyStoreJS);
	}
}
