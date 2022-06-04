/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.exceptions;

public class SequentialProvidedChallengeSourceException extends ConfigurationEngineException {

	private static final long serialVersionUID = -1115154567997726182L;

	public SequentialProvidedChallengeSourceException(final String message) {
		super(message);
	}

	public SequentialProvidedChallengeSourceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public SequentialProvidedChallengeSourceException(final Throwable cause) {
		super(cause);
	}

}
