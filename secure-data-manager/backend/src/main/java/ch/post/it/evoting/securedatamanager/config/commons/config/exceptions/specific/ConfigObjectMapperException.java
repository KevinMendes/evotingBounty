/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.specific;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.ConfigurationEngineException;

public class ConfigObjectMapperException extends ConfigurationEngineException {

	private static final long serialVersionUID = -817031132473827332L;

	public ConfigObjectMapperException(final String message) {
		super(message);
	}

	public ConfigObjectMapperException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public ConfigObjectMapperException(final Throwable cause) {
		super(cause);
	}

}
