/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.authentication;

public class AdminBoardCertificates {

	// Object representing a the certificates in json format.
	private String certificates;

	public AdminBoardCertificates(final String certificates) {
		this.certificates = certificates;
	}

	public String getCertificates() {
		return certificates;
	}

	public void setCertificates(final String certificates) {
		this.certificates = certificates;
	}
}
