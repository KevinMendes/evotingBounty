/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.extendedkeystore.constants.ExtendedKeyStoreConstants;
import ch.post.it.evoting.cryptolib.extendedkeystore.tests.BaseExtendedKeyStoreTests;
import ch.post.it.evoting.cryptolib.primitives.primes.utils.PrimitivesTestDataGenerator;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

/**
 * Utility to generate various types of Extended key store data needed by tests.
 */
public class SksTestDataGenerator extends BaseExtendedKeyStoreTests {

	private static final String ALLOWED_ALIAS_CHARACTERS = "abcedefghijklmnopqrstuvwxyz0123456789";

	/**
	 * Retrieves a pre-generated private key object from a file.
	 *
	 * @return the private key object.
	 * @throws IOException              if the private key cannot be read from the file.
	 * @throws GeneralSecurityException if the private key object cannot be created.
	 */
	public static PrivateKey getPrivateKey() throws IOException, GeneralSecurityException {

		return loadPrivateKey("privateKey.pem");
	}

	/**
	 * Retrieves a pre-generated X509 certificate object from a file.
	 *
	 * @return the X509 certificate object.
	 * @throws GeneralSecurityException if the X509 certificate object cannot be created
	 * @throws IOException              I/O error occurred.
	 */
	public static X509Certificate getX509Certificate() throws GeneralSecurityException, IOException {

		return loadX509Certificate("cert.pem");
	}

	/**
	 * Retrieves a pre-generated secret key object from a file.
	 *
	 * @return the secret key object.
	 * @throws IOException              if the secret key object cannot be read from the file.
	 * @throws GeneralSecurityException if the secret key object cannot be created.
	 */
	public static SecretKey getSecretKey() throws IOException, GeneralSecurityException {

		return loadSecretKey("secretKey.bin");
	}

	/**
	 * Retrieves a pre-generated ElGamal private key object from a file.
	 *
	 * @return the ElGamal private key object.
	 * @throws IOException               if the ElGamal private key cannot be read from the file.
	 * @throws GeneralSecurityException  if the ElGamal private key object cannot be created.
	 * @throws GeneralCryptoLibException if the ElGamal private key cannot be deserialized.
	 */
	public static ElGamalPrivateKey getElGamalPrivateKey() throws IOException, GeneralSecurityException, GeneralCryptoLibException {

		return loadElGamalPrivateKey("longElGamalPrivateKey.txt");
	}

	/**
	 * Retrieves the input stream for a pre-generated Extended key store.
	 *
	 * @return the key store input stream.
	 */
	public static InputStream getKeyStoreInputStream() {

		final String keyStoreFileName = "keystore.sks";

		return SksTestDataGenerator.class.getResourceAsStream("/" + keyStoreFileName);
	}

	/**
	 * Generates a Extended key store entry alias.
	 *
	 * @return the generated alias.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getAlias() throws GeneralCryptoLibException {

		final int numAliasChars = CommonTestDataGenerator
				.getInt(ExtendedKeyStoreConstants.MINIMUM_SKS_ENTRY_ALIAS_LENGTH, ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH);

		return CommonTestDataGenerator.getString(numAliasChars, ALLOWED_ALIAS_CHARACTERS);
	}

	/**
	 * Generates a Extended key store password.
	 *
	 * @return the generated password.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static char[] getPassword() throws GeneralCryptoLibException {

		final int numPasswordChars = CommonTestDataGenerator
				.getInt(ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH, ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH);

		return PrimitivesTestDataGenerator.getCharArray64(numPasswordChars);
	}

	/**
	 * Generates a Extended key store entry alias that only contains white spaces (for testing purposes).
	 *
	 * @return the generated alias.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getWhiteSpaceAlias() throws GeneralCryptoLibException {

		return CommonTestDataGenerator
				.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH));
	}

	/**
	 * Generates a Extended key store entry alias which contains an illegal character (for testing purposes).
	 *
	 * @return the generated alias.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getAliasWithIllegalCharacter() throws GeneralCryptoLibException {

		final String alias = CommonTestDataGenerator.getString(ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH, ALLOWED_ALIAS_CHARACTERS);

		final StringBuilder builder = new StringBuilder(alias);
		builder.setCharAt(0, 'Ç');

		return builder.toString();
	}

	/**
	 * Generates a Extended key store entry alias whose length is less than the required minimum value (for testing purposes).
	 *
	 * @return the generated alias.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getBelowMinLengthAlias() throws GeneralCryptoLibException {

		return CommonTestDataGenerator.getString(ExtendedKeyStoreConstants.MINIMUM_SKS_ENTRY_ALIAS_LENGTH - 1, ALLOWED_ALIAS_CHARACTERS);
	}

	/**
	 * Generates a Extended key store entry alias whose length is greater than the required maximum value (for testing purposes).
	 *
	 * @return the generated alias.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static String getAboveMaxLengthAlias() throws GeneralCryptoLibException {

		return CommonTestDataGenerator.getString(ExtendedKeyStoreConstants.MAXIMUM_SKS_ENTRY_ALIAS_LENGTH + 1, ALLOWED_ALIAS_CHARACTERS);
	}

	/**
	 * Generates a Extended key store password that only contains white spaces (for testing purposes).
	 *
	 * @return the generated password.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static char[] getWhiteSpacePassword() throws GeneralCryptoLibException {

		return CommonTestDataGenerator.getWhiteSpaceString(CommonTestDataGenerator.getInt(1, ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH))
				.toCharArray();
	}

	/**
	 * Generates a Extended key store password whose length is less than the required minimum value (for testing purposes).
	 *
	 * @return the generated password.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static char[] getBelowMinLengthPassword() throws GeneralCryptoLibException {

		return CommonTestDataGenerator.getString(ExtendedKeyStoreConstants.MINIMUM_SKS_PASSWORD_LENGTH - 1, ALLOWED_ALIAS_CHARACTERS).toCharArray();
	}

	/**
	 * Generates a Extended key store password whose length is greater than the required maximum value (for testing purposes).
	 *
	 * @return the generated password.
	 * @throws GeneralCryptoLibException if the random generation process fails.
	 */
	public static char[] getAboveMaxLengthPassword() throws GeneralCryptoLibException {

		return CommonTestDataGenerator.getString(ExtendedKeyStoreConstants.MAXIMUM_SKS_PASSWORD_LENGTH + 1, ALLOWED_ALIAS_CHARACTERS).toCharArray();
	}
}
