/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.ConfigurationEngineException;

public class CreateElectionEventException extends ConfigurationEngineException {

	private static final long serialVersionUID = -1270573848048325071L;

	public CreateElectionEventException(final String message) {
		super(message);
	}

	public CreateElectionEventException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CreateElectionEventException(final Throwable cause) {
		super(cause);
	}

}
