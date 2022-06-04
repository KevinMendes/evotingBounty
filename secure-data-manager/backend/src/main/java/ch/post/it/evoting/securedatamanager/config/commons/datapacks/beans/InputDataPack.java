/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.datapacks.beans;

import java.security.KeyPair;
import java.time.ZonedDateTime;

import ch.post.it.evoting.domain.election.helpers.ReplacementsHolder;

/**
 * An abstract class that defines the basic information that arrives to a generator.
 */
public abstract class InputDataPack {

	private ZonedDateTime startDate;

	private ZonedDateTime endDate;

	private String eeid;

	private KeyPair parentKeyPair;

	private ReplacementsHolder replacementsHolder;

	/**
	 * @return Returns the parentKeyPair.
	 */
	public KeyPair getParentKeyPair() {
		return parentKeyPair;
	}

	/**
	 * @param parentKeyPair The parentKeyPair to set.
	 */
	public void setParentKeyPair(final KeyPair parentKeyPair) {
		this.parentKeyPair = parentKeyPair;
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

	/**
	 * @return Returns the eeid.
	 */
	public String getEeid() {
		return eeid;
	}

	/**
	 * @param eeid The eeid to set.
	 */
	public void setEeid(final String eeid) {
		this.eeid = eeid;
	}

	/**
	 * @return Returns the replacementsHolder.
	 */
	public ReplacementsHolder getReplacementsHolder() {
		return replacementsHolder;
	}

	/**
	 * @param replacementsHolder The replacementsHolder to set.
	 */
	public void setReplacementsHolder(final ReplacementsHolder replacementsHolder) {
		this.replacementsHolder = replacementsHolder;
	}
}
