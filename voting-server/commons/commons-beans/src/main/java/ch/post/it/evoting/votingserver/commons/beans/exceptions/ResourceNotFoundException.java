/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

import javax.ejb.ApplicationException;

/**
 * Exception class for handling a resource which is not found.
 */
@ApplicationException
public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 7520190073572456145L;

	// resource which provokes the exception
	private String resource;

	// error code of the exception
	private String errorCode;

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public ResourceNotFoundException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public ResourceNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new exception with the specified detail message, resource and errorCode.
	 *
	 * @param message   The detail message.
	 * @param resource  The resource which has provoked the exception.
	 * @param errorCode The error code of the exception.
	 */
	public ResourceNotFoundException(final String message, final String resource, final String errorCode) {
		super(message);
		this.resource = resource;
		this.errorCode = errorCode;
	}

	/**
	 * Gets the value of field resource.
	 *
	 * @return the resource.
	 */
	public String getResource() {
		return resource;
	}

	/**
	 * gets the value of field errorCode.
	 *
	 * @return the field error Code
	 */
	public String getErrorCode() {
		return errorCode;
	}
}
