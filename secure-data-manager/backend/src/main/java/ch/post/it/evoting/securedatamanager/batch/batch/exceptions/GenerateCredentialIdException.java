/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;

public class GenerateCredentialIdException extends CreateVotingCardSetException {

	private static final long serialVersionUID = 5449910856687285210L;

	public GenerateCredentialIdException(final String message) {
		super(message);
	}

	public GenerateCredentialIdException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateCredentialIdException(final Throwable cause) {
		super(cause);
	}
}
