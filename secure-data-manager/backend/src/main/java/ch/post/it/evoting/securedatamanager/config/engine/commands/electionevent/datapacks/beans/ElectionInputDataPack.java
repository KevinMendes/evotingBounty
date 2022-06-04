/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.electionevent.datapacks.beans;

import java.time.ZonedDateTime;

import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans.InputDataPack;

/**
 * A class representing a inputDatapack with a credentialProperties object
 */
public class ElectionInputDataPack extends InputDataPack {

	private CredentialProperties credentialProperties;

	private ZonedDateTime electionStartDate;

	private ZonedDateTime electionEndDate;

	public CredentialProperties getCredentialProperties() {
		return credentialProperties;
	}

	public void setCredentialProperties(CredentialProperties credentialProperties) {
		this.credentialProperties = credentialProperties;
	}

	/**
	 * Gets electionStartDate.
	 *
	 * @return Value of electionStartDate.
	 */
	public ZonedDateTime getElectionStartDate() {
		return electionStartDate;
	}

	/**
	 * Sets new electionStartDate.
	 *
	 * @param electionStartDate New value of electionStartDate.
	 */
	public void setElectionStartDate(ZonedDateTime electionStartDate) {
		this.electionStartDate = electionStartDate;
	}

	/**
	 * Gets electionEndDate.
	 *
	 * @return Value of electionEndDate.
	 */
	public ZonedDateTime getElectionEndDate() {
		return electionEndDate;
	}

	/**
	 * Sets new electionEndDate.
	 *
	 * @param electionEndDate New value of electionEndDate.
	 */
	public void setElectionEndDate(ZonedDateTime electionEndDate) {
		this.electionEndDate = electionEndDate;
	}
}
