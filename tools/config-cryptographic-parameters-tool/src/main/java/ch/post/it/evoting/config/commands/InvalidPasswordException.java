/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands;

import java.security.KeyManagementException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public final class InvalidPasswordException extends KeyManagementException {

	public InvalidPasswordException(String message, Throwable cause) {
		super(message, cause);
	}
}
