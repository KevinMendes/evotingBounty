/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;

public class AuthenticationException extends Exception {

	private static final long serialVersionUID = 1L;
	private final int remainingAttempts;

	public AuthenticationException(EntryPersistenceException e, int remainingAttempts) {
		super(e);
		this.remainingAttempts = remainingAttempts;
	}

	public AuthenticationException(String string, int remainingAttempts) {
		super(string);
		this.remainingAttempts = remainingAttempts;
	}

	public int getRemainingAttempts() {
		return remainingAttempts;
	}

}
