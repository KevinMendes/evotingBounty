/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class ConfirmationMessageMathematicalGroupValidationException extends RuntimeException {

	private static final long serialVersionUID = -4091024142745802797L;

	public ConfirmationMessageMathematicalGroupValidationException(Throwable cause) {
		super(cause);
	}

	public ConfirmationMessageMathematicalGroupValidationException(String message) {
		super(message);
	}

	public ConfirmationMessageMathematicalGroupValidationException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
