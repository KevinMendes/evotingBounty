/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class VotingCardSetChoiceCodesServiceException extends RuntimeException {

	private static final long serialVersionUID = 8069770207692371130L;

	public VotingCardSetChoiceCodesServiceException(final Throwable cause) {
		super(cause);
	}

	public VotingCardSetChoiceCodesServiceException(final String message) {
		super(message);
	}

	public VotingCardSetChoiceCodesServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
