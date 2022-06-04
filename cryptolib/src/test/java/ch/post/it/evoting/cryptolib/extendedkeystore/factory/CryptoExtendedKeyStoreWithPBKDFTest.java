/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.stream.Stream;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ConfigExtendedKeyStoreTypeAndProvider;
import ch.post.it.evoting.cryptolib.extendedkeystore.tests.BaseExtendedKeyStoreTests;
import ch.post.it.evoting.cryptolib.primitives.derivation.factory.CryptoPBKDFDeriver;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipher;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;

@ExtendWith(MockitoExtension.class)
class CryptoExtendedKeyStoreWithPBKDFTest extends BaseExtendedKeyStoreTests {

	private final byte[] okSalt = new byte[] { 1 };
	private final PasswordProtection okPassword = new PasswordProtection("01234567890abcdefghijk".toCharArray());
	private final String okAlias = "myalias";
	private final String okAliasWithUnderscore = "m_y_a_l_i_a_s-";

	private CryptoExtendedKeyStoreWithPBKDF target;
	private Certificate[] okChain;
	private PrivateKey okPrivateKey;
	private byte[] okDerived;

	@Mock
	private KeyStore keystore;

	@Mock
	private SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory;

	@Mock
	private CryptoPBKDFDeriver cryptoPBKDFDeriver;

	@Mock
	private CryptoAPIDerivedKey okPbeKey;

	@Mock
	private ConfigSecretKeyAlgorithmAndSpec configSecretKeyAlgorithmAndSpec;

	@Mock
	private SymmetricAuthenticatedCipher cryptoSymmetricAuthenticatedCipher;

	@BeforeAll
	static void setupAll() {
		Security.addProvider(new BouncyCastleProvider());
	}

	@BeforeEach
	void setup() throws GeneralSecurityException, IOException, NoSuchFieldException, IllegalAccessException {

		okPrivateKey = BaseExtendedKeyStoreTests.loadPrivateKey("privateKey.pem");
		final X509Certificate okCert = BaseExtendedKeyStoreTests.loadX509Certificate("cert.pem");
		okChain = new Certificate[1];
		okChain[0] = okCert;
		okDerived = new byte[] { 1, 2, 3, 4, 5 };

		when(cryptoPBKDFDeriver.generateRandomSalt()).thenReturn(okSalt);
		when(symmetricAuthenticatedCipherFactory.create()).thenReturn(cryptoSymmetricAuthenticatedCipher);

		target = new CryptoExtendedKeyStoreWithPBKDF(ConfigExtendedKeyStoreTypeAndProvider.PKCS12_SUN_JSSE.getType(),
				ConfigExtendedKeyStoreTypeAndProvider.PKCS12_SUN_JSSE.getProvider(), cryptoPBKDFDeriver, symmetricAuthenticatedCipherFactory,
				configSecretKeyAlgorithmAndSpec);

		final Field field = CryptoExtendedKeyStoreWithPBKDF.class.getDeclaredField("keyStore");
		field.setAccessible(true);
		field.set(target, keystore);
	}

	@Test
	void setKeyEntryTest() throws GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(okPassword.getPassword(), okSalt)).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);

		assertDoesNotThrow(() -> target.setPrivateKeyEntry(okAlias, okPrivateKey, okPassword.getPassword(), okChain));
	}

	@Test
	void setNullPrivateKeyTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setSecretKeyEntry(okAlias, null, okPassword.getPassword()));
	}

	@Test
	void setKeyEntryTestWithUnderscoreAndHyphen() throws GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(okPassword.getPassword(), okSalt)).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);

		assertDoesNotThrow(() -> target.setPrivateKeyEntry(okAliasWithUnderscore, okPrivateKey, okPassword.getPassword(), okChain));
	}

	private static Stream<Arguments> getAliases() {
		return Stream.of(
				Arguments.of(""),
				Arguments.of("sfkjasdhfkjashfjasfhksjfhsjkafhkawhfkweufhskufhkuefhksufhukefhksauhfkashefukhaskefhaskuefhsk"),
				Arguments.of("sfkjasdhfkj!ahfkashefukhaskefhaskuefhsk"));
	}

	@ParameterizedTest
	@MethodSource("getAliases")
	void setKeyEntryAliasTest(final String alias) {
		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(alias, okPrivateKey, okPassword.getPassword(), okChain));
	}

	@Test
	void setKeyEntryNullAliasTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(null, okPrivateKey, okPassword.getPassword(), okChain));
	}

	@Test
	void setKeyEntryShortPasswordTest() {
		final char[] password = "a".toCharArray();

		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, password, okChain));
	}

	@Test
	void setKeyEntryLongPasswordTest() {
		final char[] password = new char[1024];
		Arrays.fill(password, 'a');

		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, password, okChain));
	}

	@Test
	void setKeyEntryEmptyPasswordTest() {
		final char[] password = "".toCharArray();

		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, password, okChain));
	}

	@Test
	void setKeyEntryNullPasswordTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, null, okChain));
	}

	@Test
	void setKeyEntryEmptyChainTest() {
		final Certificate[] emptyChain = new Certificate[0];

		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, okPassword.getPassword(), emptyChain));
	}

	@Test
	void setKeyEntryNullChainTest() {
		assertThrows(GeneralCryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, okPassword.getPassword(), null));
	}

	@Test
	void getChainWithKeyStoreExceptionTest() throws KeyStoreException {
		when(keystore.getCertificateChain(okAlias)).thenThrow(new KeyStoreException("test"));

		assertThrows(CryptoLibException.class, () -> target.getCertificateChain(okAlias));
	}

	@Test
	void getKeyWithKeyStoreExceptionTest() throws GeneralSecurityException, GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(eq(okPassword.getPassword()), any(byte[].class))).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);
		when(keystore.getKey(eq(okAlias), any(char[].class))).thenThrow(new KeyStoreException("test"));

		assertThrows(CryptoLibException.class, () -> target.getPrivateKeyEntry(okAlias, okPassword.getPassword()));
	}

	@Test
	void setKeyEntryWithExceptionTest() throws KeyStoreException, GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(eq(okPassword.getPassword()), any(byte[].class))).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);
		doThrow(new KeyStoreException("test")).when(keystore).setKeyEntry(eq(okAlias), eq(okPrivateKey), any(char[].class), eq(okChain));

		assertThrows(CryptoLibException.class, () -> target.setPrivateKeyEntry(okAlias, okPrivateKey, okPassword.getPassword(), okChain));
	}

	@Test
	void storeTest() throws IOException, GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(okPassword.getPassword(), okSalt)).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);

		try (final OutputStream mockedOut = new ByteArrayOutputStream()) {
			assertDoesNotThrow(() -> target.store(mockedOut, okPassword.getPassword()));
		}
	}

	@Test
	void storeWithExceptionTest() throws IOException, GeneralSecurityException, GeneralCryptoLibException {
		when(cryptoPBKDFDeriver.deriveKey(okPassword.getPassword(), okSalt)).thenReturn(okPbeKey);
		when(okPbeKey.getEncoded()).thenReturn(okDerived);
		doThrow(new KeyStoreException("test")).when(keystore).store(any(OutputStream.class), any(char[].class));

		try (final OutputStream mockedOut = new ByteArrayOutputStream()) {
			assertThrows(CryptoLibException.class, () -> target.store(mockedOut, okPassword.getPassword()));
		}
	}
}
