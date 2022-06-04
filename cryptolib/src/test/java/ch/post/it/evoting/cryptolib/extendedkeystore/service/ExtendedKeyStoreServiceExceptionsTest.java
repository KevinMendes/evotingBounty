/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.constants.ExtendedKeyStoreConstants;
import ch.post.it.evoting.cryptolib.extendedkeystore.utils.SksTestDataGenerator;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestCertificate;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPrivateKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestPublicKey;
import ch.post.it.evoting.cryptolib.test.tools.bean.TestSecretKey;

class ExtendedKeyStoreServiceExceptionsTest {

	private static String alias;
	private static char[] password;
	private static PasswordProtection passwordProtection;
	private static PrivateKey privateKey;
	private static Certificate certificate;
	private static Certificate[] certificateChain;
	private static SecretKey secretKey;
	private static ElGamalPrivateKey elGamalPrivateKey;
	private static byte[] emptyByteArray;
	private static char[] emptyCharArray;
	private static String whiteSpaceAlias;
	private static String aliasWithIllegalCharacter;
	private static String belowMinLengthAlias;
	private static String aboveMaxLengthAlias;
	private static char[] whiteSpacePassword;
	private static char[] belowMinLengthPassword;
	private static char[] aboveMaxLengthPassword;
	private static PasswordProtection nullPasswordProtection;
	private static PasswordProtection emptyPasswordProtection;
	private static PasswordProtection whiteSpacePasswordProtection;

	@BeforeAll
	static void setUp() throws Exception {
		Security.addProvider(new BouncyCastleProvider());
		alias = SksTestDataGenerator.getAlias();
		password = SksTestDataGenerator.getPassword();
		passwordProtection = new PasswordProtection(password);

		privateKey = SksTestDataGenerator.getPrivateKey();
		certificate = SksTestDataGenerator.getX509Certificate();
		certificateChain = new Certificate[1];
		certificateChain[0] = certificate;
		secretKey = SksTestDataGenerator.getSecretKey();
		elGamalPrivateKey = SksTestDataGenerator.getElGamalPrivateKey();

		emptyByteArray = new byte[0];
		emptyCharArray = new char[0];

		whiteSpaceAlias = SksTestDataGenerator.getWhiteSpaceAlias();
		aliasWithIllegalCharacter = SksTestDataGenerator.getAliasWithIllegalCharacter();
		belowMinLengthAlias = SksTestDataGenerator.getBelowMinLengthAlias();
		aboveMaxLengthAlias = SksTestDataGenerator.getAboveMaxLengthAlias();

		whiteSpacePassword = SksTestDataGenerator.getWhiteSpacePassword();
		belowMinLengthPassword = SksTestDataGenerator.getBelowMinLengthPassword();
		aboveMaxLengthPassword = SksTestDataGenerator.getAboveMaxLengthPassword();

		nullPasswordProtection = new PasswordProtection(null);
		emptyPasswordProtection = new PasswordProtection(emptyCharArray);
		whiteSpacePasswordProtection = new PasswordProtection(whiteSpacePassword);
	}

	static Stream<Arguments> loadExtendedKeyStore() {
		final InputStream keyStoreInputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

		return Stream.of(arguments(null, passwordProtection, "Key store input stream is null."),
				arguments(keyStoreInputStream, null, "Key store password protection is null."),
				arguments(keyStoreInputStream, nullPasswordProtection, "Key store password is null."),
				arguments(keyStoreInputStream, emptyPasswordProtection, "Key store password is blank."),
				arguments(keyStoreInputStream, whiteSpacePasswordProtection, "Key store password is blank."));
	}

	static Stream<Arguments> loadExtendedKeyStoreFromStream() {

		final InputStream keyStoreInputStream = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));

		return Stream.of(arguments(null, password, "Key store input stream is null."),
				arguments(keyStoreInputStream, null, "Key store password is null" + "."),
				arguments(keyStoreInputStream, emptyCharArray, "Key store password is blank."),
				arguments(keyStoreInputStream, whiteSpacePassword, "Key store password is blank."));
	}

	static Stream<Arguments> loadExtendedKeyStoreFromFile() {

		final Path keyStorePath = Paths.get("test");

		return Stream.of(arguments(null, password, "Key store path is null."), arguments(keyStorePath, null, "Key store password is null."),
				arguments(keyStorePath, emptyCharArray, "Key store password is blank."),
				arguments(keyStorePath, whiteSpacePassword, "Key store password is blank."));
	}

	static Stream<Arguments> loadExtendedKeyStoreFromJSON() {

		final InputStream keyStoreInStream = SksTestDataGenerator.getKeyStoreInputStream();

		return Stream.of(arguments(null, passwordProtection, "Key store JSON input stream is null."),
				arguments(keyStoreInStream, null, "Key store password protection is null."),
				arguments(keyStoreInStream, nullPasswordProtection, "Key store password is null."),
				arguments(keyStoreInStream, emptyPasswordProtection, "Key store password is blank."),
				arguments(keyStoreInStream, whiteSpacePasswordProtection, "Key store password is blank."));
	}

	static Stream<Arguments> formatExtendedKeyStoreToJSON() {
		return Stream.of(arguments(null, "Key store input stream is null."));
	}

	static Stream<Arguments> getPrivateKeyFromExtendedKeyStore() {

		return Stream.of(arguments(null, password, "Private key alias is null."), arguments("", password, "Private key alias is blank."),
				arguments(whiteSpaceAlias, password, "Private key alias is blank."), arguments(alias, null, "Private key password is null."),
				arguments(alias, emptyCharArray, "Private key password is blank."),
				arguments(alias, whiteSpacePassword, "Private key password is blank."));
	}

	static Stream<Arguments> getCertificateChainFromExtendedKeyStore() {

		return Stream.of(arguments(null, "Certificate chain alias is null."), arguments("", "Certificate chain alias is blank."),
				arguments(whiteSpaceAlias, "Certificate chain alias is blank."));
	}

	static Stream<Arguments> getSecretKeyFromExtendedKeyStore() {

		return Stream.of(arguments(null, password, "Secret key alias is null."), arguments("", password, "Secret key alias is blank."),
				arguments(whiteSpaceAlias, password, "Secret key alias is blank."), arguments(alias, null, "Secret key password is null."),
				arguments(alias, emptyCharArray, "Secret key password is blank."),
				arguments(alias, whiteSpacePassword, "Secret key password is blank."));
	}

	static Stream<Arguments> getElGamalPrivateKeyFromExtendedKeyStore() {

		return Stream.of(arguments(null, password, "ElGamal private key alias is null."),
				arguments("", password, "ElGamal private key alias is blank."),
				arguments(whiteSpaceAlias, password, "ElGamal private key alias is blank."),
				arguments(alias, null, "ElGamal private key password is null."),
				arguments(alias, emptyCharArray, "ElGamal private key password is blank."),
				arguments(alias, whiteSpacePassword, "ElGamal private key password is blank."));
	}

	static Stream<Arguments> setPrivateKeyEntryInExtendedKeyStore() {

		final Certificate[] chainContainingNullCert = new Certificate[2];
		chainContainingNullCert[0] = certificate;
		chainContainingNullCert[1] = null;

		final Certificate[] chainContainingNullContentCert = chainContainingNullCert.clone();
		chainContainingNullContentCert[1] = new TestCertificate("test", new TestPublicKey(null));

		final Certificate[] chainContainingEmptyContentCert = chainContainingNullCert.clone();
		chainContainingEmptyContentCert[1] = new TestCertificate("test", new TestPublicKey(emptyByteArray));

		return Stream.of(arguments(null, privateKey, password, certificateChain, "Private key alias is null."),
				arguments("", privateKey, password, certificateChain, "Private key alias is blank."),
				arguments(whiteSpaceAlias, privateKey, password, certificateChain, "Private key alias is blank."),
				arguments(belowMinLengthAlias, privateKey, password, certificateChain, "Private key alias is blank."),
				arguments(aboveMaxLengthAlias, privateKey, password, certificateChain,
						"Private key alias length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH
								+ "; Found " + aboveMaxLengthAlias.length()),
				arguments(aliasWithIllegalCharacter, privateKey, password, certificateChain,
						"Private key alias contains characters outside of allowed set "
								+ ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS),
				arguments(alias, null, password, certificateChain, "Private key is null."),
				arguments(alias, new TestPrivateKey(null), password, certificateChain, "Private key content is null."),
				arguments(alias, new TestPrivateKey(emptyByteArray), password, certificateChain, "Private key content is empty."),
				arguments(alias, privateKey, null, certificateChain, "Private key password is null."),
				arguments(alias, privateKey, emptyCharArray, certificateChain, "Private key password is blank."),
				arguments(alias, privateKey, whiteSpacePassword, certificateChain, "Private key password is blank."),
				arguments(alias, privateKey, belowMinLengthPassword, certificateChain,
						"Private key password length must be greater than or equal to : " + ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH
								+ "; Found " + belowMinLengthPassword.length), arguments(alias, privateKey, aboveMaxLengthPassword, certificateChain,
						"Private key password length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH
								+ "; Found " + aboveMaxLengthPassword.length),
				arguments(alias, privateKey, password, null, "Certificate chain is null."),
				arguments(alias, privateKey, password, new Certificate[0], "Certificate chain is empty."),
				arguments(alias, privateKey, password, chainContainingNullCert, "Certificate in chain is null."),
				arguments(alias, privateKey, password, chainContainingNullContentCert, "Content of certificate in chain is null."),
				arguments(alias, privateKey, password, chainContainingEmptyContentCert, "Content of certificate in chain is empty."));
	}

	static Stream<Arguments> setSecretKeyEntryInExtendedKeyStore() {

		return Stream.of(arguments(null, secretKey, password, "Secret key alias is null."),
				arguments("", secretKey, password, "Secret key alias is blank."),
				arguments(whiteSpaceAlias, secretKey, password, "Secret key alias is blank."),
				arguments(belowMinLengthAlias, secretKey, password, "Secret key alias is blank."), arguments(aboveMaxLengthAlias, secretKey, password,
						"Secret key alias length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH
								+ "; Found " + aboveMaxLengthAlias.length()), arguments(aliasWithIllegalCharacter, secretKey, password,
						"Secret key alias contains characters outside of allowed set "
								+ ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS),
				arguments(alias, null, password, "Secret key is null."),
				arguments(alias, new TestSecretKey(null), password, "Secret key content is null."),
				arguments(alias, new TestSecretKey(emptyByteArray), password, "Secret key content is empty."),
				arguments(alias, secretKey, null, "Secret key password is null."),
				arguments(alias, secretKey, emptyCharArray, "Secret key password is blank."),
				arguments(alias, secretKey, whiteSpacePassword, "Secret key password is blank."), arguments(alias, secretKey, belowMinLengthPassword,
						"Secret key password length must be greater than or equal to : " + ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH
								+ "; Found " + belowMinLengthPassword.length), arguments(alias, secretKey, aboveMaxLengthPassword,
						"Secret key password length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH
								+ "; Found " + aboveMaxLengthPassword.length));
	}

	static Stream<Arguments> setElGamalPrivateKeyEntryInExtendedKeyStore() {

		return Stream.of(arguments(null, elGamalPrivateKey, password, "ElGamal private key alias is null."),
				arguments("", elGamalPrivateKey, password, "ElGamal private key alias is blank."),
				arguments(whiteSpaceAlias, elGamalPrivateKey, password, "ElGamal private key alias is blank."),
				arguments(belowMinLengthAlias, elGamalPrivateKey, password, "ElGamal private key alias is blank."),
				arguments(aboveMaxLengthAlias, elGamalPrivateKey, password,
						"ElGamal private key alias length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH
								+ "; Found " + aboveMaxLengthAlias.length()), arguments(aliasWithIllegalCharacter, elGamalPrivateKey, password,
						"ElGamal private key alias contains characters outside of allowed set "
								+ ExtendedKeyStoreConstants.ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS),
				arguments(alias, null, password, "ElGamal private key is null."),
				arguments(alias, elGamalPrivateKey, null, "ElGamal private key password is null."),
				arguments(alias, elGamalPrivateKey, emptyCharArray, "ElGamal private key password is blank."),
				arguments(alias, elGamalPrivateKey, whiteSpacePassword, "ElGamal private key password is blank."),
				arguments(alias, elGamalPrivateKey, belowMinLengthPassword, "ElGamal private key password length must be greater than or equal to : "
						+ ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH + "; Found " + belowMinLengthPassword.length),
				arguments(alias, elGamalPrivateKey, aboveMaxLengthPassword,
						"ElGamal private key password length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH
								+ "; Found " + aboveMaxLengthPassword.length));
	}

	static Stream<Arguments> storeExtendedKeyStore() throws IOException {

		try (final OutputStream keyStoreOutStream = new ByteArrayOutputStream()) {

			return Stream.of(arguments(null, password, "Key store output stream is null."),
					arguments(keyStoreOutStream, null, "Key store password is null."),
					arguments(keyStoreOutStream, emptyCharArray, "Key store password is blank."),
					arguments(keyStoreOutStream, whiteSpacePassword, "Key store password is blank."),
					arguments(keyStoreOutStream, belowMinLengthPassword,
							"Key store password length must be greater than or equal to : " + ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH
									+ "; Found " + belowMinLengthPassword.length), arguments(keyStoreOutStream, aboveMaxLengthPassword,
							"Key store password length must be less than or equal to : " + ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH
									+ "; Found " + aboveMaxLengthPassword.length));
		}
	}

	@ParameterizedTest
	@MethodSource("loadExtendedKeyStore")
	void testExtendedKeyStoreLoadExceptions(final InputStream in, final PasswordProtection password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().loadKeyStore(in, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("loadExtendedKeyStoreFromStream")
	void testExtendedKeyStoreLoadFromStreamExceptions(final InputStream in, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().loadKeyStore(in, password));
		assertEquals(errorMsg, exception.getMessage());

	}

	@ParameterizedTest
	@MethodSource("loadExtendedKeyStoreFromFile")
	void testExtendedKeyStoreLoadFromFileExceptions(final Path path, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().loadKeyStore(path, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("loadExtendedKeyStoreFromJSON")
	void testExtendedKeyStoreLoadFromJSONExceptions(final InputStream in, final PasswordProtection password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().loadKeyStoreFromJSON(in, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("formatExtendedKeyStoreToJSON")
	void testFormatExtendedKeyStoreToJSONExceptions(final InputStream in, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().formatKeyStoreToJSON(in));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getPrivateKeyFromExtendedKeyStore")
	void testGetPrivateKeyFromExtendedKeyStoreExceptions(final String alias, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().getPrivateKeyEntry(alias, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getCertificateChainFromExtendedKeyStore")
	void testGetCertificateChainFromExtendedKeyStoreExceptions(final String alias, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().getCertificateChain(alias));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getSecretKeyFromExtendedKeyStore")
	void testGetSecretKeyFromExtendedKeyStoreExceptions(final String alias, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().getSecretKeyEntry(alias, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("getElGamalPrivateKeyFromExtendedKeyStore")
	void testGetElGamalPrivateKeyFromExtendedKeyStoreExceptions(final String alias, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().getElGamalPrivateKeyEntry(alias, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setPrivateKeyEntryInExtendedKeyStore")
	void testSetPrivateKeyEntryInExtendedKeyStoreExceptions(final String alias, final PrivateKey privateKey, final char[] password, final Certificate[] chain,
			final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().setPrivateKeyEntry(alias, privateKey, password, chain));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setSecretKeyEntryInExtendedKeyStore")
	void testSetSecretKeyEntryInExtendedKeyStoreExceptions(final String alias, final SecretKey secretKey, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().setSecretKeyEntry(alias, secretKey, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("setElGamalPrivateKeyEntryInExtendedKeyStore")
	void testSetElGamalPrivateKeyEntryInExtendedKeyStoreExceptions(final String alias, final ElGamalPrivateKey privateKey, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().setElGamalPrivateKeyEntry(alias, privateKey, password));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("storeExtendedKeyStore")
	void testExtendedKeyStoreStorageExceptions(final OutputStream out, final char[] password, final String errorMsg) {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> new ExtendedKeyStoreService().createKeyStore().store(out, password));
		assertEquals(errorMsg, exception.getMessage());
	}

}
