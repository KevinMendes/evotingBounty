/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.handler;

import java.nio.charset.StandardCharsets;
import java.security.KeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingServiceAPI;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SmartcardException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.PrivateKeySerializer;
import ch.post.it.evoting.securedatamanager.config.shares.shares.service.SmartCardService;

/**
 * Stateless implementation of a read shares handler.
 */
public class StatelessReadSharesHandler {

	private final PrivateKeySerializer privateKeySerializer;

	private final SmartCardService smartcardService;

	private final AsymmetricServiceAPI asymmetricServiceAPI;

	private final ThresholdSecretSharingServiceAPI thresholdSecretSharingServiceAPI;

	public StatelessReadSharesHandler(final PrivateKeySerializer privateKeySerializer, final SmartCardService smartcardService,
			final AsymmetricServiceAPI asymmetricService, final ThresholdSecretSharingServiceAPI thresholdSecretSharingServiceAPI) {
		this.privateKeySerializer = privateKeySerializer;
		this.smartcardService = smartcardService;
		this.asymmetricServiceAPI = asymmetricService;
		this.thresholdSecretSharingServiceAPI = thresholdSecretSharingServiceAPI;
	}

	public Share readShare(final String pin, final PublicKey issuerPublicKey) throws SharesException {
		final Share share;
		try {
			share = smartcardService.read(pin, issuerPublicKey);
		} catch (final SmartcardException e) {
			throw new SharesException("An error occurred while reading the smartcard", e);
		}

		return share;
	}

	/**
	 * Returns a base64 encoded string, which contains the serialized share. The issuer public key is used to verify the signature of the smartcard.
	 */
	public String readShareAndStringify(final String pin, final PublicKey issuerPublicKey) throws SharesException {
		final Share share = readShare(pin, issuerPublicKey);
		final byte[] serializedShare = thresholdSecretSharingServiceAPI.serialize(share);

		return new String(Base64.getEncoder().encode(serializedShare), StandardCharsets.UTF_8);
	}

	public PrivateKey getPrivateKey(final Set<Share> shares, final PublicKey subjectPublicKey) throws SharesException {
		final PrivateKey privateKey;
		try {

			final byte[] recovered = thresholdSecretSharingServiceAPI.recover(shares);
			privateKey = privateKeySerializer.reconstruct(recovered, subjectPublicKey);

			validateKeyPair(subjectPublicKey, privateKey);
		} catch (final KeyException | GeneralCryptoLibException e) {
			throw new SharesException("There was an error reconstructing the private key from the shares", e);
		}

		return privateKey;
	}

	/**
	 * Reconstructs key given a set of serialized shares in base64 encoded format, and the corresponding public key.
	 */
	public PrivateKey getPrivateKeyWithSerializedShares(final Set<String> serializedShares, final PublicKey subjectPublicKey) throws SharesException {

		final Set<Share> shares = new HashSet<>();

		for (final String serializedShare : serializedShares) {
			final byte[] shareBytes = Base64.getDecoder().decode(serializedShare);
			final Share share;
			try {
				share = thresholdSecretSharingServiceAPI.deserialize(shareBytes);
			} catch (final GeneralCryptoLibException e) {
				throw new SharesException("There was an error while deserializing the shares", e);
			}
			shares.add(share);
		}

		return getPrivateKey(shares, subjectPublicKey);
	}

	public String getSmartcardLabel() throws SharesException {
		try {
			return smartcardService.readSmartcardLabel();
		} catch (final SmartcardException e) {
			throw new SharesException("Error while trying to read the smartcard label", e);
		}
	}

	private void validateKeyPair(final PublicKey subjectPublicKey, final PrivateKey privateKey) throws SharesException {

		final String testString = "foobar";

		final byte[] encryptedTestString;
		final byte[] decrypted;

		try {
			encryptedTestString = asymmetricServiceAPI.encrypt(subjectPublicKey, testString.getBytes(StandardCharsets.UTF_8));
			decrypted = asymmetricServiceAPI.decrypt(privateKey, encryptedTestString);
		} catch (final GeneralCryptoLibException e) {
			throw new SharesException("There was an error while validating the reconstructed private key with the given public key", e);
		}
		final String decryptedTestString = new String(decrypted, StandardCharsets.UTF_8);

		if (!testString.equals(decryptedTestString)) {
			throw new SharesException("There was an error validating the reconstructed private key with the given public key");
		}

	}

}
