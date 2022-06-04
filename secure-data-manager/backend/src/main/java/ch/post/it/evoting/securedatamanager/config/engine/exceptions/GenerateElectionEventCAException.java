/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.exceptions;

public class GenerateElectionEventCAException extends CreateElectionEventException {

	private static final long serialVersionUID = 7632515886602651521L;

	public GenerateElectionEventCAException(final String message) {
		super(message);
	}

	public GenerateElectionEventCAException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateElectionEventCAException(final Throwable cause) {
		super(cause);
	}
}
