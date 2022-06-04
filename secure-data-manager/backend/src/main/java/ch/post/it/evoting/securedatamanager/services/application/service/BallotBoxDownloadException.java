/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

public class BallotBoxDownloadException extends Exception {

	public BallotBoxDownloadException(final String message) {
		super(message);
	}

	public BallotBoxDownloadException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
