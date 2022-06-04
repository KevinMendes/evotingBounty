/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.tenant;

/**
 * Encapsulates a single piece of data.
 */
public class TenantInstallationData {

	private String encodedData;

	/**
	 * Gets the value of the encoded data
	 *
	 * @return
	 */
	public String getEncodedData() {
		return encodedData;
	}

	/**
	 * Sets a new value for the encoded data
	 *
	 * @param encodedData
	 */
	public void setEncodedData(final String encodedData) {
		this.encodedData = encodedData;
	}
}
