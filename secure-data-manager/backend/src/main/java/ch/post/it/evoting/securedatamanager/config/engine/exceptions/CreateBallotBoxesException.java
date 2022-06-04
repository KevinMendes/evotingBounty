/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.exceptions;

import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.ConfigurationEngineException;

public class CreateBallotBoxesException extends ConfigurationEngineException {

	private static final long serialVersionUID = 770571259291263625L;

	public CreateBallotBoxesException(final String message) {
		super(message);
	}

	public CreateBallotBoxesException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CreateBallotBoxesException(final Throwable cause) {
		super(cause);
	}

}
