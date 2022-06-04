/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.exceptions;

public class GenerateAdminBoardKeysException extends CreateElectionEventException {

	private static final long serialVersionUID = -6892894405026386682L;

	public GenerateAdminBoardKeysException(final String message) {
		super(message);
	}

	public GenerateAdminBoardKeysException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateAdminBoardKeysException(final Throwable cause) {
		super(cause);
	}
}
