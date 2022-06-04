/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class ConsistencyCheckException extends Exception {

	private static final long serialVersionUID = 4330290741872284503L;

	public ConsistencyCheckException(final String msg) {
		super(msg);
	}

	public ConsistencyCheckException(final Throwable t) {
		super(t);
	}

	public ConsistencyCheckException(final String msg, final Throwable t) {
		super(msg, t);
	}
}
