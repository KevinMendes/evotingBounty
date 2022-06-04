/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.exception;

public class VotingCardSetDataGeneratorServiceException extends RuntimeException {

	private static final long serialVersionUID = 315280617446598532L;

	public VotingCardSetDataGeneratorServiceException(Throwable cause) {
		super(cause);
	}

	public VotingCardSetDataGeneratorServiceException(String message) {
		super(message);
	}

	public VotingCardSetDataGeneratorServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}
