/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.utils;

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
	public String encryptPasswordIfEncryptionKeyAvailable(final String plaintextPassword, final String publicKeyToBeUsedToEncryptThePassword)
			throws GeneralCryptoLibException {

		if (publicKeyToBeUsedToEncryptThePassword == null || publicKeyToBeUsedToEncryptThePassword.isEmpty()) {

			return plaintextPassword;

		} else {

			final PublicKey publicKey = PemUtils.publicKeyFromPem(publicKeyToBeUsedToEncryptThePassword);

			return Base64.getEncoder().encodeToString(asymmetricService.encrypt(publicKey, plaintextPassword.getBytes(StandardCharsets.UTF_8)));
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
	public String decryptPassword(final String encryptedPassword, final String privateKeyAsPem) throws GeneralCryptoLibException {

		return decryptPassword(encryptedPassword, PemUtils.privateKeyFromPem(privateKeyAsPem));
	}

	/**
	 * Decrpyt the received encrypted password using the received private key.
	 *
	 * @param encryptedPassword the encrypted password to be decrypted.
	 * @param privateKey        a private key.
	 * @return the decrypted password.
	 * @throws GeneralCryptoLibException
	 */
	public String decryptPassword(final String encryptedPassword, final PrivateKey privateKey) throws GeneralCryptoLibException {

		final byte[] ciphertextAsBytes = Base64.getDecoder().decode(encryptedPassword);

		final byte[] plaintextAsBytes = asymmetricService.decrypt(privateKey, ciphertextAsBytes);

		return new String(plaintextAsBytes, StandardCharsets.UTF_8);
	}
}
