/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.service;

import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SmartcardException;

public interface SmartCardService {

	/**
	 * Write a share into the smartCard
	 *
	 * @param share             the {@link Share} to write.
	 * @param name
	 * @param oldPinPuk         the old pin and puk code.
	 * @param newPinPuk         the new pin and puk code.
	 * @param signingPrivateKey the {@link PrivateKey} used to sign.
	 * @throws SmartcardException
	 */
	void write(final Share share, final String name, final String oldPinPuk, final String newPinPuk, final PrivateKey signingPrivateKey)
			throws SmartcardException;

	/**
	 * Read a share from the smart card
	 *
	 * @param pin                            the smart card pin code.
	 * @param signatureVerificationPublicKey
	 * @return the read {@link Share}.
	 * @throws SmartcardException
	 */
	Share read(final String pin, final PublicKey signatureVerificationPublicKey) throws SmartcardException;

	/**
	 * Checks the status of the inserted smartcard.
	 *
	 * @return true if the smartcard status is satisfactory, false otherwise
	 */
	boolean isSmartcardOk();

	/**
	 * Read the smartcard label
	 *
	 * @return the label written to the smartcard
	 * @throws SmartcardException
	 */
	String readSmartcardLabel() throws SmartcardException;

}
