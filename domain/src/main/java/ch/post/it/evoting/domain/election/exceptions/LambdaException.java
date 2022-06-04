/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.exceptions;

/**
 * Exception to be caught after invoking a lambda expression.
 */
public class LambdaException extends RuntimeException {

	private static final long serialVersionUID = 1347248014980109621L;

	// resource which provokes the exception
	private final Exception cause;

	/**
	 * Constructs a new lambda exception with the specified exception as cause.
	 *
	 * @param cause - the exception which is cause of this lambda exception.
	 */
	public LambdaException(final Exception cause) {
		super();
		this.cause = cause;
	}

	/**
	 * Returns the current value of the field cause.
	 *
	 * @return Returns the cause.
	 */
	@Override
	public synchronized Exception getCause() {
		return cause;
	}

}
