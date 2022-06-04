/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

/**
 * Exception thrown by the application layer.
 */
@javax.ejb.ApplicationException
public class ApplicationException extends Exception {

	private static final long serialVersionUID = 3222357906625609063L;

	// resource which provokes the expection
	private String resource;

	// error code of the exception
	private String errorCode;

	// field which provokes the exception
	private String field;

	/**
	 * Constructs a new exception that wraps
	 *
	 * @param cause the underlying cause of the exception.
	 */
	public ApplicationException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public ApplicationException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public ApplicationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message,resource and errorCode.
	 *
	 * @param message   The detail message.
	 * @param resource  The resource which has provoked the exception.
	 * @param errorCode The error code of the exception.
	 * @param field     The field affected by the error.
	 */
	public ApplicationException(final String message, final String resource, final String errorCode, final String field) {
		super(message);
		this.resource = resource;
		this.errorCode = errorCode;
		this.field = field;
	}

	/**
	 * Gets the value of field resource.
	 *
	 * @return
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * gets the value of field errorCode.
	 *
	 * @return
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * Return the field which provoked the Exception.
	 *
	 * @return
	 */
	public String getField() {
		return field;
	}
}
