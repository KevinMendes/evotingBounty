/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class ChoiceCodesComputationServiceException extends RuntimeException {

	private static final long serialVersionUID = 4050170916637297678L;

	public ChoiceCodesComputationServiceException(final Throwable cause) {
		super(cause);
	}

	public ChoiceCodesComputationServiceException(final String message) {
		super(message);
	}

	public ChoiceCodesComputationServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
