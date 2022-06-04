/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.application;

public class TenantActivatorPasswordException extends RuntimeException {

	private static final long serialVersionUID = -8525920351401677634L;

	public TenantActivatorPasswordException(Throwable cause) {
		super(cause);
	}

	public TenantActivatorPasswordException(String message) {
		super(message);
	}

	public TenantActivatorPasswordException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
