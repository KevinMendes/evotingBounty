/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class VoterInformationRepositoryImplException extends RuntimeException {

	private static final long serialVersionUID = 8365942281410289817L;

	public VoterInformationRepositoryImplException(String message) {
		super(message);
	}

	public VoterInformationRepositoryImplException(Throwable cause) {
		super(cause);
	}

	public VoterInformationRepositoryImplException(String message, Throwable cause) {
		super(message, cause);
	}

}
