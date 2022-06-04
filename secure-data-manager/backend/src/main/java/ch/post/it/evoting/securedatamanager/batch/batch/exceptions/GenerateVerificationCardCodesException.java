/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;

public class GenerateVerificationCardCodesException extends CreateVotingCardSetException {

	private static final long serialVersionUID = -6464334697497064225L;

	public GenerateVerificationCardCodesException(final String message) {
		super(message);
	}

	public GenerateVerificationCardCodesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateVerificationCardCodesException(final Throwable cause) {
		super(cause);
	}
}
