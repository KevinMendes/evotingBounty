/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.exception;

public class VotingCardSetUploadRepositoryException extends RuntimeException {

	private static final long serialVersionUID = 7085337598214927323L;

	public VotingCardSetUploadRepositoryException(Throwable cause) {
		super(cause);
	}

	public VotingCardSetUploadRepositoryException(String message) {
		super(message);
	}

	public VotingCardSetUploadRepositoryException(String message, Throwable cause) {
		super(message, cause);
	}

}
