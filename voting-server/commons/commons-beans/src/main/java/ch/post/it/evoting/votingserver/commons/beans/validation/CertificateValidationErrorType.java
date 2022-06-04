/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

public enum CertificateValidationErrorType {

	IS_CERTIFICATE_AUTHORITY("The certificate to be validated should not be a CA"),

	CHAIN_VALIDATION_FAILED("The validation of the chain has failed due the following reasons"),

	CERTIFICATE_VALIDATION_FAILED("The validation of the certificate has failed due the following reasons");

	private final String description;

	CertificateValidationErrorType(final String description) {
		this.description = description;
	}

	/**
	 * Gets description.
	 *
	 * @return Value of description.
	 */
	public String getDescription() {
		return description;
	}
}
