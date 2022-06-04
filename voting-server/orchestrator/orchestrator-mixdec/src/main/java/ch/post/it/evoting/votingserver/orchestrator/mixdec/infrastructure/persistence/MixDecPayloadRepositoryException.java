/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence;

public class MixDecPayloadRepositoryException extends Exception {

	private static final long serialVersionUID = 6828919932084028638L;

	public MixDecPayloadRepositoryException(Throwable cause) {
		super(cause);
	}

	public MixDecPayloadRepositoryException(String message) {
		super(message);
	}

	public MixDecPayloadRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
