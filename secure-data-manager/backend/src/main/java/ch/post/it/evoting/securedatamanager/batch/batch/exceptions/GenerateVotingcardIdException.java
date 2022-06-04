/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;

public class GenerateVotingcardIdException extends CreateVotingCardSetException {

	private static final long serialVersionUID = -5270520282485321378L;

	public GenerateVotingcardIdException(final String message) {
		super(message);
	}

	public GenerateVotingcardIdException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateVotingcardIdException(final Throwable cause) {
		super(cause);
	}
}
