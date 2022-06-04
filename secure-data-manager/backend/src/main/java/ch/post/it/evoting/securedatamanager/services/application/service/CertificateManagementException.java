/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

/**
 * An exception while retrieving or storing a certificate.
 */
public class CertificateManagementException extends Exception {

	private static final long serialVersionUID = 1588022932761467427L;

	public CertificateManagementException(final Throwable cause) {
		super(cause);
	}
}
