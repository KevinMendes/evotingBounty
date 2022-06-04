/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote;

/**
 * Exception used when the remote resource exists but it's not in the available state
 */
public class ResourceNotReadyException extends Exception {
	private static final long serialVersionUID = 1380313381728852937L;

	public ResourceNotReadyException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
