/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.CreateVotingCardSetException;

public class GenerateAuthenticationValuesException extends CreateVotingCardSetException {

	private static final long serialVersionUID = 3880165186665546553L;

	public GenerateAuthenticationValuesException(final String message) {
		super(message);
	}

	public GenerateAuthenticationValuesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public GenerateAuthenticationValuesException(final Throwable cause) {
		super(cause);
	}

}
