/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import static java.util.Arrays.fill;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;

/**
 * Allows a password (string) to be encrypted and decrypted.
 */
public final class PasswordEncrypter {

	private final AsymmetricServiceAPI asymmetricService;

	public PasswordEncrypter(final AsymmetricServiceAPI asymmetricService) {

		this.asymmetricService = asymmetricService;
	}

	/**
	 * Encrypts the received password using the private key.
	 * <p>
	 * If the received private key is {@code null} or an empty string, then this method will return the original password.
	 *
	 * @param plaintextPassword                     the password to be encrypted.
	 * @param publicKeyToBeUsedToEncryptThePassword the public key to be used to encrypt the password (in PEM format).
	 * @return the encrypted password.
	 * @throws GeneralCryptoLibException
	 */
	public String encryptPasswordIfEncryptionKeyAvailable(final char[] plaintextPassword, final String publicKeyToBeUsedToEncryptThePassword)
			throws GeneralCryptoLibException {

		if (publicKeyToBeUsedToEncryptThePassword == null || publicKeyToBeUsedToEncryptThePassword.isEmpty()) {

			return new String(plaintextPassword);

		} else {

			final PublicKey publicKey = PemUtils.publicKeyFromPem(publicKeyToBeUsedToEncryptThePassword);
			final CharBuffer charBuffer = CharBuffer.wrap(plaintextPassword);
			final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
			final byte[] bytes = new byte[byteBuffer.remaining()];
			byteBuffer.get(bytes);
			String encryptedPassword;
			try {
				encryptedPassword = Base64.getEncoder().encodeToString(asymmetricService.encrypt(publicKey, bytes));
			} finally {
				fill(byteBuffer.array(), (byte) 0);
				fill(bytes, (byte) 0);
			}
			return encryptedPassword;
		}
	}

	/**
	 * Decrypts the received encrypted password using the received private key (represented as a string in PEM format).
	 *
	 * @param encryptedPassword the encrypted password to be decrypted.
	 * @param privateKeyAsPem   private key in PEM format.
	 * @return the decrypted password.
	 * @throws GeneralCryptoLibException
	 */
	public char[] decryptPassword(final String encryptedPassword, final String privateKeyAsPem) throws GeneralCryptoLibException {

		return decryptPassword(encryptedPassword, PemUtils.privateKeyFromPem(privateKeyAsPem));
	}

	/**
	 * Decrypt the received encrypted password using the received private key.
	 *
	 * @param encryptedPassword the encrypted password to be decrypted.
	 * @param privateKey        a private key.
	 * @return the decrypted password.
	 * @throws GeneralCryptoLibException
	 */
	public char[] decryptPassword(final String encryptedPassword, final PrivateKey privateKey) throws GeneralCryptoLibException {

		final byte[] ciphertextAsBytes = Base64.getDecoder().decode(encryptedPassword);

		final ByteBuffer byteBuffer = ByteBuffer.wrap(asymmetricService.decrypt(privateKey, ciphertextAsBytes));
		char[] plaintextPassword;
		try {
			final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
			try {
				plaintextPassword = new char[charBuffer.remaining()];
				charBuffer.get(plaintextPassword);
			} finally {
				fill(charBuffer.array(), ' ');
			}
		} finally {
			fill(byteBuffer.array(), (byte) 0);
		}
		return plaintextPassword;
	}
}
