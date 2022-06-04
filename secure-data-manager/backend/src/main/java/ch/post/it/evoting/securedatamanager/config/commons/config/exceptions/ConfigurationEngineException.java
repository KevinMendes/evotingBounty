/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.exceptions;

public class ConfigurationEngineException extends RuntimeException {

	private static final long serialVersionUID = 4444578007782610068L;

	public ConfigurationEngineException(final String message) {
		super(message);
	}

	public ConfigurationEngineException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ConfigurationEngineException(final Throwable cause) {
		super(cause);
	}
}
