/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

/**
 * Tests of the stores service API.
 */
class StoresServiceTest {

	private static final String TEST_DATA_PATH = "target" + File.separator + "cryptolib-stores-test-data";

	private static final String PKCS12_KEY_STORE_PATH = TEST_DATA_PATH + File.separator + "CryptoLibStoresModuleTestPkcs12.p12";

	private static StoresService _storesServiceForDefaultPolicy;

	private static PrivateKey _rootPrivateKey;

	private static PublicKey _rootPublicKey;

	private static PrivateKey _privateKey;

	private static CryptoAPIX509Certificate _rootCertificate;

	private static CryptoAPIX509Certificate _certificate;

	private static char[] _rootPrivateKeyPassword;

	private static char[] _privateKeyPassword;

	private static String _rootPrivateKeyAlias;

	private static String _privateKeyAlias;

	private static char[] _keyStorePassword;

	private static KeyStore _pkcs12KeyStore;

	private static KeyStore _pkcs12KeyStoreLoaded;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException, KeyStoreException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, NoSuchAlgorithmException, CertificateException, IOException {

		final File outputDataDir = new File(TEST_DATA_PATH);
		if (!outputDataDir.exists()) {
			outputDataDir.mkdirs();
		}

		_storesServiceForDefaultPolicy = new StoresService();
		final Field field = StoresService.class.getDeclaredField("provider");
		field.setAccessible(true);
		field.set(_storesServiceForDefaultPolicy, Provider.SUN);

		_pkcs12KeyStore = _storesServiceForDefaultPolicy.createKeyStore(KeyStoreType.PKCS12);

		final KeyPair rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		_rootPublicKey = rootKeyPair.getPublic();
		_rootPrivateKey = rootKeyPair.getPrivate();
		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		_privateKey = keyPair.getPrivate();

		_rootCertificate = X509CertificateTestDataGenerator.getRootAuthorityX509Certificate(rootKeyPair);
		_certificate = X509CertificateTestDataGenerator.getSignX509Certificate(keyPair, rootKeyPair);

		final int numChars = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH);

		_rootPrivateKeyPassword = PrimitivesTestDataGenerator.getCharArray64(numChars);
		_privateKeyPassword = PrimitivesTestDataGenerator.getCharArray64(numChars);
		_keyStorePassword = PrimitivesTestDataGenerator.getCharArray64(numChars);

		_rootPrivateKeyAlias = PrimitivesTestDataGenerator.getString32(numChars);
		_privateKeyAlias = PrimitivesTestDataGenerator.getString32(numChars);

		final X509Certificate[] rootCertificateChain = new X509Certificate[1];
		rootCertificateChain[0] = _rootCertificate.getCertificate();
		_pkcs12KeyStore.setKeyEntry(_rootPrivateKeyAlias, _rootPrivateKey, _rootPrivateKeyPassword, rootCertificateChain);

		final X509Certificate[] certificateChain = new X509Certificate[2];
		certificateChain[0] = _certificate.getCertificate();
		certificateChain[1] = _rootCertificate.getCertificate();
		_pkcs12KeyStore.setKeyEntry(_privateKeyAlias, _privateKey, _privateKeyPassword, certificateChain);

		try (final FileOutputStream outStream = new FileOutputStream(PKCS12_KEY_STORE_PATH)) {
			_pkcs12KeyStore.store(outStream, _keyStorePassword);
		}

		try (final FileInputStream inStream = new FileInputStream(PKCS12_KEY_STORE_PATH)) {
			_pkcs12KeyStoreLoaded = _storesServiceForDefaultPolicy.loadKeyStore(KeyStoreType.PKCS12, inStream, _keyStorePassword);
		}
	}

	@AfterAll
	public static void tearDown() {

		final File outputDataDir = new File(TEST_DATA_PATH);
		if (!outputDataDir.exists()) {
			outputDataDir.mkdirs();
		}
	}

	@Test
	final void whenCreatePkcs12CanRetrieveRootPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		final Key rootPrivateKey = _pkcs12KeyStore.getKey(_rootPrivateKeyAlias, _rootPrivateKeyPassword);

		Assertions.assertArrayEquals(rootPrivateKey.getEncoded(), _rootPrivateKey.getEncoded());
	}

	@Test
	final void whenCreatePkcs12CanRetrieveRootCertificateChain()
			throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
			SignatureException {

		final Certificate[] rootCertificateChain = _pkcs12KeyStore.getCertificateChain(_rootPrivateKeyAlias);

		final Certificate rootCertificate = rootCertificateChain[0];

		rootCertificate.verify(_rootPublicKey);

		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}

	@Test
	final void whenCreatePkcs12CanRetrievePrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		final Key privateKey = _pkcs12KeyStore.getKey(_privateKeyAlias, _privateKeyPassword);

		Assertions.assertArrayEquals(privateKey.getEncoded(), _privateKey.getEncoded());
	}

	@Test
	final void whenCreatePkcs12CanRetrieveCertificateChain()
			throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
			SignatureException {

		final Certificate[] certificateChain = _pkcs12KeyStore.getCertificateChain(_privateKeyAlias);

		final Certificate certificate = certificateChain[0];
		final Certificate rootCertificate = certificateChain[1];

		certificate.verify(_rootPublicKey);
		rootCertificate.verify(_rootPublicKey);

		Assertions.assertArrayEquals(certificate.getEncoded(), _certificate.getEncoded());
		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}

	@Test
	final void whenCreatePkcs12WithBcProviderThenCreationIsSuccessful()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, GeneralCryptoLibException,
			KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, InvalidKeyException, CertificateException,
			NoSuchProviderException, SignatureException {

		final Field field = StoresService.class.getDeclaredField("provider");
		field.setAccessible(true);
		field.set(_storesServiceForDefaultPolicy, Provider.BOUNCY_CASTLE);
		final KeyStore pkcs12KeyStore = _storesServiceForDefaultPolicy.createKeyStore(KeyStoreType.PKCS12);

		final X509Certificate[] rootCertificateChain = new X509Certificate[1];
		rootCertificateChain[0] = _rootCertificate.getCertificate();
		pkcs12KeyStore.setKeyEntry(_rootPrivateKeyAlias, _rootPrivateKey, _rootPrivateKeyPassword, rootCertificateChain);

		final Key privateKey = pkcs12KeyStore.getKey(_rootPrivateKeyAlias, _rootPrivateKeyPassword);
		Assertions.assertArrayEquals(privateKey.getEncoded(), _rootPrivateKey.getEncoded());

		final Certificate[] certificateChain = _pkcs12KeyStore.getCertificateChain(_rootPrivateKeyAlias);
		final Certificate rootCertificate = certificateChain[0];
		rootCertificate.verify(_rootPublicKey);
		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}

	@Test
	final void whenLoadPkcs12CanRetrieveRootPrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		final Key privateKey = _pkcs12KeyStoreLoaded.getKey(_rootPrivateKeyAlias, _rootPrivateKeyPassword);

		Assertions.assertArrayEquals(privateKey.getEncoded(), _rootPrivateKey.getEncoded());
	}

	@Test
	final void whenLoadPkcs12CanRetrieveRootCertificateChain()
			throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
			SignatureException {

		final Certificate[] certificateChain = _pkcs12KeyStoreLoaded.getCertificateChain(_rootPrivateKeyAlias);

		final Certificate rootCertificate = certificateChain[0];

		rootCertificate.verify(_rootPublicKey);

		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}

	@Test
	final void whenLoadPkcs12CanRetrievePrivateKey() throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {

		final Key privateKey = _pkcs12KeyStoreLoaded.getKey(_privateKeyAlias, _privateKeyPassword);

		Assertions.assertArrayEquals(privateKey.getEncoded(), _privateKey.getEncoded());
	}

	@Test
	final void whenLoadPkcs12CanRetrieveCertificateChain()
			throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException,
			SignatureException {

		final Certificate[] certificateChain = _pkcs12KeyStoreLoaded.getCertificateChain(_privateKeyAlias);

		final Certificate certificate = certificateChain[0];
		final Certificate rootCertificate = certificateChain[1];

		certificate.verify(_rootPublicKey);
		rootCertificate.verify(_rootPublicKey);

		Assertions.assertArrayEquals(certificate.getEncoded(), _certificate.getEncoded());
		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}

	@Test
	final void whenLoadPkcs12WithBcProviderThenLoadIsSuccessful()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, GeneralCryptoLibException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException,
			NoSuchProviderException, SignatureException {

		final Field field = StoresService.class.getDeclaredField("provider");
		field.setAccessible(true);
		field.set(_storesServiceForDefaultPolicy, Provider.BOUNCY_CASTLE);
		KeyStore pkcs12KeyStore = _storesServiceForDefaultPolicy.createKeyStore(KeyStoreType.PKCS12);

		final X509Certificate[] rootCertificateChain = new X509Certificate[1];
		rootCertificateChain[0] = _rootCertificate.getCertificate();
		pkcs12KeyStore.setKeyEntry(_rootPrivateKeyAlias, _rootPrivateKey, _rootPrivateKeyPassword, rootCertificateChain);

		try (final FileOutputStream outStream = new FileOutputStream(PKCS12_KEY_STORE_PATH)) {
			pkcs12KeyStore.store(outStream, _keyStorePassword);
		}

		try (final FileInputStream inStream = new FileInputStream(PKCS12_KEY_STORE_PATH)) {
			pkcs12KeyStore = _storesServiceForDefaultPolicy.loadKeyStore(KeyStoreType.PKCS12, inStream, _keyStorePassword);
		}

		final Key privateKey = pkcs12KeyStore.getKey(_rootPrivateKeyAlias, _rootPrivateKeyPassword);
		Assertions.assertArrayEquals(privateKey.getEncoded(), _rootPrivateKey.getEncoded());

		final Certificate[] certificateChain = pkcs12KeyStore.getCertificateChain(_rootPrivateKeyAlias);
		final Certificate rootCertificate = certificateChain[0];
		rootCertificate.verify(_rootPublicKey);
		Assertions.assertArrayEquals(rootCertificate.getEncoded(), _rootCertificate.getEncoded());
	}
}
