/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.service;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.securedatamanager.config.shares.shares.EncryptedShare;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.NoSmartcardFoundException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SmartcardException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.WrongPinException;
import ch.post.it.evoting.securedatamanager.config.shares.token.AcquireTokenTask;
import ch.post.it.evoting.securedatamanager.config.shares.token.GuiOutput;
import ch.post.it.evoting.securedatamanager.config.shares.token.ModuleManager;
import ch.post.it.evoting.securedatamanager.config.shares.token.TokenOps;

import iaik.pkcs.pkcs11.Module;
import iaik.pkcs.pkcs11.Token;
import iaik.pkcs.pkcs11.TokenException;

/**
 * Implementation of the SmartCard Service
 */
class DefaultSmartCardService implements SmartCardService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSmartCardService.class);

	private final TokenOps tokenOps = new TokenOps();

	@Override
	public synchronized void write(final Share share, final String name, final String oldPinPuk, final String newPinPuk,
			final PrivateKey signingPrivateKey) throws SmartcardException {
		final Token token = acquireToken();
		try {
			final EncryptedShare encryptedShare = new EncryptedShare(share, signingPrivateKey);
			tokenOps.writeShare(token, oldPinPuk.toCharArray(), newPinPuk.toCharArray(), name, encryptedShare);
		} catch (final TokenException e) {
			throw new SmartcardException("Error while writing the token to the smartcard", e);
		}
	}

	@Override
	public synchronized Share read(final String pin, final PublicKey signatureVerificationPublicKey) throws SmartcardException {
		final Token token = acquireToken();
		final Share shareRead;
		try {
			shareRead = tokenOps.readShare(token, pin.toCharArray(), signatureVerificationPublicKey);
		} catch (final TokenException | SharesException | IllegalArgumentException e) {
			// Due to bad design of TokenOps, IllegalArgumentException here means that
			// the share or it's signature are missing in the smartcard.
			throw new SmartcardException("There was an error reading the smartcard", e);
		}
		if (shareRead == null) {
			throw new WrongPinException();
		}
		return shareRead;
	}

	@Override
	public synchronized boolean isSmartcardOk() {
		// Check if a token can be retrieved for the inserted smartcard
		return tryAcquireToken() != null;
	}

	@Override
	public synchronized String readSmartcardLabel() throws SmartcardException {
		final Token token = acquireToken();
		try {
			// Label is trimmed as it is padded to the max label size (32
			// chars)
			return token.getTokenInfo().getLabel().trim();
		} catch (final TokenException e) {
			throw new SmartcardException("There was an error reading the smartcard label", e);
		}
	}

	private Token acquireToken() throws NoSmartcardFoundException {
		final Token token = tryAcquireToken();
		if (token == null) {
			throw new NoSmartcardFoundException();
		}
		return token;
	}

	private Token tryAcquireToken() {
		final Module module = ModuleManager.INSTANCE.getModule();
		final GuiOutput output = new GuiOutput() {

			@Override
			public void tooManyTokens(final int nTokens) {
				LOGGER.info("Too many tokens detected, number of tokens: {}", nTokens);
			}

			@Override
			public void noTokenPresent() {
				LOGGER.info("No token detected");
			}
		};
		return new AcquireTokenTask(module, output, false).call();
	}
}
