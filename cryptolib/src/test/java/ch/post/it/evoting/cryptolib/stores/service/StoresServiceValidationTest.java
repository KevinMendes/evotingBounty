/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class StoresServiceValidationTest {

	private static final String TEST_DATA_PATH = "target" + File.separator + "cryptolib-stores-test-data";
	private static final String PKCS12_KEY_STORE_PATH = TEST_DATA_PATH + File.separator + "CryptoLibStoresModuleTestPkcs12.p12";

	private static StoresService storesServiceForDefaultPolicy;
	private static char[] keyStorePassword;
	private static String whiteSpaceString;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		storesServiceForDefaultPolicy = new StoresService();

		final int numChars = CommonTestDataGenerator.getInt(1, SecureRandomConstants.MAXIMUM_GENERATED_STRING_LENGTH);

		final char[] privateKeyPassword = PrimitivesTestDataGenerator.getCharArray64(numChars);
		final String privateKeyAlias = PrimitivesTestDataGenerator.getString32(numChars);

		keyStorePassword = PrimitivesTestDataGenerator.getCharArray64(numChars);

		setUpKeyStore(privateKeyPassword, privateKeyAlias, keyStorePassword);

		whiteSpaceString = CommonTestDataGenerator.getWhiteSpaceString(numChars);
	}

	static Stream<Arguments> createKeyStore() {
		return Stream.of(arguments(null, "Key store type is null."));
	}

	static Stream<Arguments> loadKeyStore() throws IOException {

		final FileInputStream keyStoreInStream = new FileInputStream(PKCS12_KEY_STORE_PATH);

		return Stream.of(arguments(null, keyStoreInStream, keyStorePassword, "Key store type is null."),
				arguments(KeyStoreType.PKCS12, null, keyStorePassword, "Key store input stream is null."),
				arguments(KeyStoreType.PKCS12, keyStoreInStream, null, "Key store password is null."),
				arguments(KeyStoreType.PKCS12, keyStoreInStream, "".toCharArray(), "Key store password is blank."),
				arguments(KeyStoreType.PKCS12, keyStoreInStream, whiteSpaceString.toCharArray(), "Key store password is blank."),
				arguments(KeyStoreType.PKCS12, keyStoreInStream, "wrong_password".toCharArray(), "Could not load key store."),
				arguments(KeyStoreType.JCEKS, keyStoreInStream, keyStorePassword, "Could not load key store."));
	}

	private static void setUpKeyStore(final char[] privateKeyPassword, final String privateKeyAlias, final char[] keyStorePassword)
			throws GeneralCryptoLibException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

		final File outputDataDir = new File(TEST_DATA_PATH);
		if (!outputDataDir.exists()) {
			outputDataDir.mkdirs();
		}

		storesServiceForDefaultPolicy = new StoresService();

		final KeyStore pkcs12KeyStore = storesServiceForDefaultPolicy.createKeyStore(KeyStoreType.PKCS12);

		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		final PrivateKey privateKey = keyPair.getPrivate();

		final CryptoAPIX509Certificate certificate = X509CertificateTestDataGenerator.getRootAuthorityX509Certificate(keyPair);

		final X509Certificate[] certificateChain = new X509Certificate[1];
		certificateChain[0] = certificate.getCertificate();
		pkcs12KeyStore.setKeyEntry(privateKeyAlias, privateKey, privateKeyPassword, certificateChain);

		final FileOutputStream outStream = new FileOutputStream(PKCS12_KEY_STORE_PATH);
		pkcs12KeyStore.store(outStream, keyStorePassword);
	}

	@ParameterizedTest
	@MethodSource("createKeyStore")
	void testKeyStoreCreationValidation(final KeyStoreType type, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> storesServiceForDefaultPolicy.createKeyStore(type));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("loadKeyStore")
	void testKeyStoreLoadingValidation(final KeyStoreType type, final InputStream inStream, final char[] password, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> storesServiceForDefaultPolicy.loadKeyStore(type, inStream, password));
		assertEquals(errorMsg, exception.getMessage());
	}

}
