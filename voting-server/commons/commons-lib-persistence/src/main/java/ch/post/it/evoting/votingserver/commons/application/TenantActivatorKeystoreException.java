/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.application;

public class TenantActivatorKeystoreException extends RuntimeException {

	private static final long serialVersionUID = 1920022423620443456L;

	public TenantActivatorKeystoreException(Throwable cause) {
		super(cause);
	}

	public TenantActivatorKeystoreException(String message) {
		super(message);
	}

	public TenantActivatorKeystoreException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
