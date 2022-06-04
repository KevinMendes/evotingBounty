/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.tests.BaseExtendedKeyStoreTests;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

@ExtendWith(MockitoExtension.class)
class ExtendedKeyStoreServiceIT extends BaseExtendedKeyStoreTests {

	private static final String ALIAS_1 = "alias1";
	private static final String ALIAS_2 = "alias2";

	private static ElGamalPrivateKey elGamalPrivateKey1;
	private static ElGamalPrivateKey elGamalPrivateKey2;

	private final ExtendedKeyStoreService keyStoreService = new ExtendedKeyStoreService();

	@BeforeAll
	public static void init() throws GeneralCryptoLibException {

		final GroupLoader zpGroupLoader = new GroupLoader();
		final BigInteger p = zpGroupLoader.getP();
		final BigInteger q = zpGroupLoader.getQ();
		final BigInteger g = zpGroupLoader.getG();
		final ZpSubgroup zpSubgroup = new ZpSubgroup(g, p, q);

		elGamalPrivateKey1 = getElGamalPrivateKeyEntry(zpSubgroup, Arrays.asList(new Exponent(q, g), new Exponent(q, g.add(BigInteger.TEN))));

		elGamalPrivateKey2 = getElGamalPrivateKeyEntry(zpSubgroup,
				Arrays.asList(new Exponent(q, g.subtract(BigInteger.ONE)), new Exponent(q, g.subtract(BigInteger.TEN))));
	}

	private static String readFile(final String path) throws IOException {
		final StringBuilder content = new StringBuilder();
		try (final InputStream stream = ExtendedKeyStoreServiceIT.class.getClassLoader().getResourceAsStream(path);
				final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			int c;
			while ((c = reader.read()) != -1) {
				content.append((char) c);
			}
		}
		return content.toString();
	}

	private static ElGamalPrivateKey getElGamalPrivateKeyEntry(final ZpSubgroup zpSubgroup, final List<Exponent> keyExponentList)
			throws GeneralCryptoLibException {
		return new ElGamalPrivateKey(keyExponentList, zpSubgroup);
	}

	@Test
	void createP12WithPBKDF() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");

		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;

		final String alias = "myalias_-";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		final CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setPrivateKeyEntry(alias, privateKey, password.getPassword(), chain);

		final List<String> privateKeyAliases = cryptoKeyStore.getPrivateKeyAliases();

		assertTrue(privateKeyAliases.contains(alias));

		final List<String> secretKeyAliases = cryptoKeyStore.getSecretKeyAliases();

		assertTrue(secretKeyAliases.isEmpty());

		try (final FileOutputStream outKeyStore = new FileOutputStream("target/keystore.sks")) {
			cryptoKeyStore.store(outKeyStore, password.getPassword());
		}

		assertNotNull(cryptoKeyStore);
	}

	@Test
	void openSksWithSoreBiggerThan10k() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");

		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;

		final String alias = "myalias_";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		final CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		final int privateKeyCount = 50;
		for (int i = 0; i < privateKeyCount; i++) {
			cryptoKeyStore.setPrivateKeyEntry(alias + i, privateKey, password.getPassword(), chain);
		}
		final CryptoAPIExtendedKeyStore cryptoKeyStoreOpened;
		try (final OutputStream outputStream = new FileOutputStream("target/bigSks.sks");
				final InputStream inputStream = new FileInputStream("target/bigSks.sks")) {
			cryptoKeyStore.store(outputStream, password.getPassword());
			cryptoKeyStoreOpened = keyStoreService.loadKeyStore(inputStream, password);
		}

		assertNotNull(cryptoKeyStoreOpened);

		final List<String> aliases = cryptoKeyStoreOpened.getPrivateKeyAliases();

		assertEquals(privateKeyCount, aliases.size());
	}

	@Test
	void retrieveKeyWithoutStoreCall() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");

		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;

		final String alias = "myalias_-";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		final CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setPrivateKeyEntry(alias, privateKey, password.getPassword(), chain);

		final PrivateKey key = cryptoKeyStore.getPrivateKeyEntry(alias, password.getPassword());

		try (final OutputStream outKeyStore = new FileOutputStream("target/keystore.sks")) {
			cryptoKeyStore.store(outKeyStore, password.getPassword());
		}

		assertNotNull(cryptoKeyStore);

		assertEquals(privateKey, key);
	}

	@Test
	void loadKeystoreAndFromStreamAndFromFile() throws GeneralCryptoLibException, IOException, URISyntaxException {

		final String alias = "myalias_-";
		final char[] password = "01234567890abcdefghijk".toCharArray();
		final PasswordProtection passwordProtection = new PasswordProtection(password);
		final Key key;
		final Key keyFromStream;
		final Key keyFromFile;

		final CryptoAPIExtendedKeyStore keystore;
		final CryptoAPIExtendedKeyStore keystoreFromStream;
		final CryptoAPIExtendedKeyStore keystoreFromFile;

		try (final InputStream in = getClass().getResourceAsStream("/keystore.sks")) {

			keystore = keyStoreService.loadKeyStore(in, passwordProtection);
			key = keystore.getPrivateKeyEntry(alias, password);
		}

		try (final InputStream in = getClass().getResourceAsStream("/keystore.sks")) {

			keystoreFromStream = keyStoreService.loadKeyStore(in, password);
			keyFromStream = keystoreFromStream.getPrivateKeyEntry(alias, password);
		}

		final Path path = Paths.get(getClass().getResource("/keystore.sks").toURI());
		keystoreFromFile = keyStoreService.loadKeyStore(path, password);
		keyFromFile = keystoreFromFile.getPrivateKeyEntry(alias, password);

		assertEquals(key, keyFromStream);
		assertEquals(key, keyFromFile);
	}

	@Test
	void loadP12WithPBKDF() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;

		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");

		final String alias = "myalias_-";

		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());
		final Key key;
		final CryptoAPIExtendedKeyStore keystore;
		try (final InputStream in = getClass().getResourceAsStream("/keystore.sks")) {

			keystore = keyStoreService.loadKeyStore(in, password);

			final List<String> privateKeyAliases = keystore.getPrivateKeyAliases();

			assertTrue(privateKeyAliases.contains(alias));

			key = keystore.getPrivateKeyEntry(alias, password.getPassword());
		}

		assertEquals(privateKey, key);
		assertArrayEquals(chain, keystore.getCertificateChain(alias));
	}

	@Test
	void secretKeyTest() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final SecretKey secretKey = BaseExtendedKeyStoreTests.loadSecretKey("secretKey.bin");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");
		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;
		List<String> privateKeyAliases;
		List<String> secretKeyAliases;

		final String alias = "myalias";
		final String symmetricAlias_1 = "myaliassymmetric_";
		final String symmetricAlias_2 = "myaliassymmetric-2_";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setPrivateKeyEntry(alias, privateKey, password.getPassword(), chain);

		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_1, secretKey, password.getPassword());
		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_2, secretKey, password.getPassword());

		privateKeyAliases = cryptoKeyStore.getPrivateKeyAliases();

		assertTrue(privateKeyAliases.contains(alias));
		assertFalse(privateKeyAliases.contains(symmetricAlias_1));

		secretKeyAliases = cryptoKeyStore.getSecretKeyAliases();

		assertTrue(secretKeyAliases.contains(symmetricAlias_1));
		assertTrue(secretKeyAliases.contains(symmetricAlias_2));
		assertFalse(secretKeyAliases.contains(alias));

		try (final OutputStream outKeyStore = new FileOutputStream("target/keystoreSymmetric.sks")) {
			cryptoKeyStore.store(outKeyStore, password.getPassword());
		}

		assertNotNull(cryptoKeyStore);

		try (final InputStream in = new FileInputStream("target/keystoreSymmetric.sks")) {
			cryptoKeyStore = keyStoreService.loadKeyStore(in, password);
		}

		privateKeyAliases = cryptoKeyStore.getPrivateKeyAliases();

		assertTrue(privateKeyAliases.contains(alias));
		assertFalse(privateKeyAliases.contains(symmetricAlias_1));

		secretKeyAliases = cryptoKeyStore.getSecretKeyAliases();

		assertTrue(secretKeyAliases.contains(symmetricAlias_1));
		assertTrue(secretKeyAliases.contains(symmetricAlias_2));
		assertFalse(secretKeyAliases.contains(alias));

		final Key key = cryptoKeyStore.getSecretKeyEntry(symmetricAlias_1, password.getPassword());

		assertEquals(secretKey, key);
	}

	@Test
	void elGamalPrivateKeyTest() throws IOException, GeneralCryptoLibException {

		final PasswordProtection storePass = new PasswordProtection("01234567890abvgdeyozh".toCharArray());

		final PasswordProtection keyPass = new PasswordProtection("abvgdeyozh01234567890".toCharArray());

		CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setElGamalPrivateKeyEntry(ALIAS_1, elGamalPrivateKey1, keyPass.getPassword());

		List<String> aliases = cryptoKeyStore.getElGamalPrivateKeyAliases();

		assertTrue(aliases.contains(ALIAS_1));
		assertEquals(1, aliases.size());

		cryptoKeyStore.setElGamalPrivateKeyEntry(ALIAS_2, elGamalPrivateKey2, keyPass.getPassword());

		aliases = cryptoKeyStore.getElGamalPrivateKeyAliases();

		assertTrue(aliases.contains(ALIAS_1));
		assertTrue(aliases.contains(ALIAS_2));
		assertEquals(2, aliases.size());

		try (final OutputStream outKeyStore = new FileOutputStream("target/extended_keystore.sks")) {
			cryptoKeyStore.store(outKeyStore, storePass.getPassword());
		}

		assertNotNull(cryptoKeyStore);

		try (final InputStream in = new FileInputStream("target/extended_keystore.sks")) {
			cryptoKeyStore = keyStoreService.loadKeyStore(in, storePass);
		}

		aliases = cryptoKeyStore.getElGamalPrivateKeyAliases();

		assertTrue(aliases.contains(ALIAS_1));
		assertTrue(aliases.contains(ALIAS_2));
		assertEquals(2, aliases.size());
		final ElGamalPrivateKey key1 = cryptoKeyStore.getElGamalPrivateKeyEntry(ALIAS_1, keyPass.getPassword());
		final ElGamalPrivateKey key2 = cryptoKeyStore.getElGamalPrivateKeyEntry(ALIAS_2, keyPass.getPassword());
		assertEquals(elGamalPrivateKey1, key1);
		assertEquals(elGamalPrivateKey2, key2);
	}

	@Test
	void longElGamalPrivateKeyTest() throws IOException, GeneralCryptoLibException {

		final ElGamalPrivateKey longElGamalPrivateKey = ElGamalPrivateKey.fromJson(readFile("longElGamalPrivateKey.txt"));

		final PasswordProtection storePass = new PasswordProtection("01234567890abvgdeyozh".toCharArray());

		final PasswordProtection keyPass = new PasswordProtection("abvgdeyozh01234567890".toCharArray());

		CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setElGamalPrivateKeyEntry(ALIAS_1, longElGamalPrivateKey, keyPass.getPassword());

		try (final FileOutputStream outKeyStore = new FileOutputStream("target/extended_keystore.sks")) {
			cryptoKeyStore.store(outKeyStore, storePass.getPassword());
		}

		final ElGamalPrivateKey key;
		try (final FileInputStream in = new FileInputStream("target/extended_keystore.sks")) {
			cryptoKeyStore = keyStoreService.loadKeyStore(in, storePass);
			key = cryptoKeyStore.getElGamalPrivateKeyEntry(ALIAS_1, keyPass.getPassword());
		}

		assertEquals(longElGamalPrivateKey, key);
	}

	@Test
	void jsonTest() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final SecretKey secretKey = BaseExtendedKeyStoreTests.loadSecretKey("secretKey.bin");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");
		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;
		final List<String> privateKeyAliases;

		final String alias = "myalias";
		final String symmetricAlias_1 = "myaliassymmetric_";
		final String symmetricAlias_2 = "myaliassymmetric-2_";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		final CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setPrivateKeyEntry(alias, privateKey, password.getPassword(), chain);

		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_1, secretKey, password.getPassword());
		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_2, secretKey, password.getPassword());

		privateKeyAliases = cryptoKeyStore.getPrivateKeyAliases();

		assertTrue(privateKeyAliases.contains(alias));
		assertFalse(privateKeyAliases.contains(symmetricAlias_1));

		assertNotNull(cryptoKeyStore);

		checkPrivateKey(cryptoKeyStore.toJSON(password.getPassword()), password, alias, privateKey);

		try (final FileOutputStream outKeyStore = new FileOutputStream("target/keystoreSymmetric.sks")) {
			cryptoKeyStore.store(outKeyStore, password.getPassword());
		}
	}

	@Test
	void formatKeystoreToJsonTest() throws GeneralSecurityException, IOException, GeneralCryptoLibException {

		final SecretKey secretKey = BaseExtendedKeyStoreTests.loadSecretKey("secretKey.bin");
		final PrivateKey privateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");
		final X509Certificate cert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		final Certificate[] chain = new Certificate[1];
		chain[0] = cert;

		final String alias = "myalias";
		final String symmetricAlias_1 = "myaliassymmetric_";
		final String symmetricAlias_2 = "myaliassymmetric-2_";
		final PasswordProtection password = new PasswordProtection("01234567890abcdefghijk".toCharArray());

		final CryptoAPIExtendedKeyStore cryptoKeyStore = keyStoreService.createKeyStore();
		cryptoKeyStore.setPrivateKeyEntry(alias, privateKey, password.getPassword(), chain);

		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_1, secretKey, password.getPassword());
		cryptoKeyStore.setSecretKeyEntry(symmetricAlias_2, secretKey, password.getPassword());
		cryptoKeyStore.setElGamalPrivateKeyEntry(ALIAS_1, elGamalPrivateKey1, password.getPassword());

		try (final FileOutputStream outKeyStore = new FileOutputStream("target/keystoreSymmetric.sks")) {
			cryptoKeyStore.store(outKeyStore, password.getPassword());
		}
		final String formatKeyStoreToJSON;
		try (final FileInputStream fileInputStream = new FileInputStream("target/keystoreSymmetric.sks")) {
			formatKeyStoreToJSON = keyStoreService.formatKeyStoreToJSON(fileInputStream);
		}
		final CryptoAPIExtendedKeyStore loadedKeyStore;
		try (final InputStream stream = new ByteArrayInputStream(formatKeyStoreToJSON.getBytes(StandardCharsets.UTF_8))) {
			loadedKeyStore = keyStoreService.loadKeyStoreFromJSON(stream, password);
		}
		assertEquals(privateKey, loadedKeyStore.getPrivateKeyEntry(alias, password.getPassword()));
		assertEquals(secretKey, loadedKeyStore.getSecretKeyEntry(symmetricAlias_1, password.getPassword()));
		assertEquals(secretKey, loadedKeyStore.getSecretKeyEntry(symmetricAlias_2, password.getPassword()));
		assertEquals(elGamalPrivateKey1, loadedKeyStore.getElGamalPrivateKeyEntry(ALIAS_1, password.getPassword()));
	}

	private void checkPrivateKey(final String json, final PasswordProtection password, final String alias, final PrivateKey privateKey)
			throws GeneralCryptoLibException {
		final byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		final CryptoAPIExtendedKeyStore store;
		try (final InputStream stream = new ByteArrayInputStream(bytes)) {
			store = keyStoreService.loadKeyStoreFromJSON(stream, password);
		} catch (final IOException e) {
			throw new AssertionError(e);
		}
		final Key key = store.getPrivateKeyEntry(alias, password.getPassword());
		assertEquals(privateKey, key);
	}

	@Test
	void testElGamalPrivateKeyToAndFromJSON() throws GeneralCryptoLibException, IOException {
		final CryptoAPIExtendedKeyStore keyStore = keyStoreService.createKeyStore();

		final PasswordProtection storePass = new PasswordProtection("01234567890abvgdeyozh".toCharArray());

		final PasswordProtection keyPass = new PasswordProtection("abvgdeyozh01234567890".toCharArray());

		keyStore.setElGamalPrivateKeyEntry(ALIAS_1, elGamalPrivateKey1, keyPass.getPassword());
		keyStore.setElGamalPrivateKeyEntry(ALIAS_2, elGamalPrivateKey2, keyPass.getPassword());

		final CryptoAPIExtendedKeyStore actualKeyStore;
		try (final ByteArrayInputStream inStream = new ByteArrayInputStream(keyStore.toJSON(storePass.getPassword()).getBytes(StandardCharsets.UTF_8))) {

			actualKeyStore = keyStoreService.loadKeyStoreFromJSON(inStream, storePass);
		}

		assertEquals(elGamalPrivateKey1, actualKeyStore.getElGamalPrivateKeyEntry(ALIAS_1, keyPass.getPassword()));
		assertEquals(elGamalPrivateKey2, actualKeyStore.getElGamalPrivateKeyEntry(ALIAS_2, keyPass.getPassword()));
	}
}
