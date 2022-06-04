/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.SecretKey;

import org.apache.commons.codec.binary.Hex;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific.GenerateAuthenticationValuesException;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationDerivedElement;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.StartVotingKey;

@Service
@JobScope
public class AuthenticationKeyCryptoService {

	@Autowired
	PrimitivesServiceAPI primitivesService;

	@Autowired
	SymmetricServiceAPI symmetricService;

	/**
	 * Derives a element from an authentication key for a given salt and election event id
	 *
	 * @param salt            - Constant element used for derivation purposes
	 * @param electionEventId - election event identifier
	 * @param value           - authentication key
	 * @return the derived element as String
	 */
	public AuthenticationDerivedElement deriveElement(final String salt, final String electionEventId, final String value) {

		try {

			final CryptoAPIPBKDFDeriver pbkdfDeriver = primitivesService.getPBKDFDeriver();
			final String composedSalt = salt.concat(electionEventId);
			final byte[] hashedSalt = primitivesService.getHash(composedSalt.getBytes(StandardCharsets.UTF_8));
			final CryptoAPIDerivedKey cryptoAPIDerivedKey = pbkdfDeriver.deriveKey(value.toCharArray(), hashedSalt);
			final byte[] keyIdBytes = cryptoAPIDerivedKey.getEncoded();
			return AuthenticationDerivedElement.of(cryptoAPIDerivedKey, new String(Hex.encodeHex(keyIdBytes)));
		} catch (final GeneralCryptoLibException e) {
			throw new GenerateAuthenticationValuesException(e);
		}
	}

	/**
	 * Encrypt a StartVoting Key for a given password
	 *
	 * @param svk
	 * @param derivedPassword
	 * @return the encrypted start voting key in Base64 format
	 */
	public String encryptSVK(final StartVotingKey svk, final AuthenticationDerivedElement derivedPassword) {

		try {

			final SecretKey secretKeyForEncryption = symmetricService.getSecretKeyForEncryptionFromDerivedKey(derivedPassword.getDerivedKey());
			final byte[] encryptedValue = symmetricService.encrypt(secretKeyForEncryption, svk.getValue().getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encryptedValue);
		} catch (final GeneralCryptoLibException e) {
			throw new GenerateAuthenticationValuesException(e);
		}
	}

	/**
	 * Decrypt a StartVoting Key for a given password
	 *
	 * @param encryptedSVK
	 * @param derivedPassword
	 * @return the encrypted start voting key in Base64 format
	 */
	public String decryptSVK(final String encryptedSVK, final AuthenticationDerivedElement derivedPassword) {

		try {

			final SecretKey secretKeyForEncryption = symmetricService.getSecretKeyForEncryptionFromDerivedKey(derivedPassword.getDerivedKey());
			final byte[] decryptedValue = symmetricService.decrypt(secretKeyForEncryption, Base64.getDecoder().decode(encryptedSVK));
			return new String(decryptedValue, StandardCharsets.UTF_8);
		} catch (final GeneralCryptoLibException e) {
			throw new GenerateAuthenticationValuesException(e);
		}
	}
}
