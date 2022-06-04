/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.exceptions;

public class GenerateBallotBoxesException extends CreateBallotBoxesException {

	private static final long serialVersionUID = 770571259291263625L;

	public GenerateBallotBoxesException(final String message) {
		super(message);
	}

	public GenerateBallotBoxesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateBallotBoxesException(final Throwable cause) {
		super(cause);
	}

}
