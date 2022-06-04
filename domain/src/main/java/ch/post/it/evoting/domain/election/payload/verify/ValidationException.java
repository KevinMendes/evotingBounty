/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.payload.verify;

import java.util.HashSet;
import java.util.Set;

import javax.validation.ConstraintViolation;

/**
 * Exception class for handling a resource which is not found.
 */
public class ValidationException extends Exception {

	private static final long serialVersionUID = 1938526782459549399L;

	// violations
	private Set<ConstraintViolation<Object>> constraintViolations = null;

	// resource which provokes the exception
	private String resource;

	// error code of the exception
	private String errorCode;

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param constraintViolations the constraint violations.
	 */
	public ValidationException(final Set<? extends ConstraintViolation<Object>> constraintViolations) {
		if (constraintViolations != null) {
			this.constraintViolations = new HashSet<>(constraintViolations);
		}
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param constraintViolations the constraint violations
	 * @param message              the detail message.
	 */
	public ValidationException(final Set<ConstraintViolation<Object>> constraintViolations, final String message) {
		super(message);
		if (constraintViolations != null) {
			this.constraintViolations = new HashSet<>(constraintViolations);
		}
	}

	/**
	 * Constructs a new exception with the specified detail message, resource and errorCode.
	 *
	 * @param constraintViolations the constraint violations
	 * @param message              The detail message.
	 * @param resource             The resource which has provoked the exception.
	 * @param errorCode            The error code of the exception.
	 */
	public ValidationException(final Set<ConstraintViolation<Object>> constraintViolations, final String message, final String resource, final String errorCode) {
		super(message);
		this.resource = resource;
		this.errorCode = errorCode;
		if (constraintViolations != null) {
			this.constraintViolations = new HashSet<>(constraintViolations);
		}
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

	/**
	 * Set of constraint violations reported during a validation.
	 *
	 * @return {@code Set} of {@link ConstraintViolation}
	 */
	public Set<ConstraintViolation<Object>> getConstraintViolations() {
		return constraintViolations;
	}

	/**
	 * This is mainly used in logging, so we can add some extra info to help us see what the problem was
	 *
	 * @return
	 */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ValidationException{");
		sb.append("message='").append(errorCode).append('\'');
		sb.append(", errorCode='").append(errorCode).append('\'');
		sb.append(", resource='").append(resource).append('\'');
		sb.append(", constraintViolations=").append(constraintViolations);
		sb.append('}');
		return sb.toString();
	}
}
