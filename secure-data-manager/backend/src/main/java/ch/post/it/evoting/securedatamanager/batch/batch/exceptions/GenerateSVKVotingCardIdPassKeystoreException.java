/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;

public class GenerateSVKVotingCardIdPassKeystoreException extends CreateVotingCardSetException {

	private static final long serialVersionUID = -9025319883880413518L;

	public GenerateSVKVotingCardIdPassKeystoreException(final String message) {
		super(message);
	}

	public GenerateSVKVotingCardIdPassKeystoreException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateSVKVotingCardIdPassKeystoreException(final Throwable cause) {
		super(cause);
	}
}
