/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans;

import java.security.KeyPair;
import java.time.ZonedDateTime;

import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;

/**
 * A class that represents a credentialDataPack with a keyPair and a certificate
 */
public class ElectionCredentialDataPack extends CredentialDataPack {

	private KeyPair keyPair;

	private CryptoAPIX509Certificate certificate;

	private ZonedDateTime startDate;

	private ZonedDateTime endDate;

	public KeyPair getKeyPair() {
		return keyPair;
	}

	public void setKeyPair(final KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public CryptoAPIX509Certificate getCertificate() {
		return certificate;
	}

	public void setCertificate(final CryptoAPIX509Certificate certificate) {
		this.certificate = certificate;
	}

	/**
	 * @return Returns the startDate.
	 */
	public ZonedDateTime getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate The startDate to set.
	 */
	public void setStartDate(final ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return Returns the endDate.
	 */
	public ZonedDateTime getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate The endDate to set.
	 */
	public void setEndDate(final ZonedDateTime endDate) {
		this.endDate = endDate;
	}

}
