/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

import java.util.Set;

import javax.validation.ConstraintViolation;

import ch.post.it.evoting.domain.election.payload.verify.ValidationException;

/**
 * Exception class that can be thrown when there are validation exceptions.
 */
public class SemanticErrorException extends ValidationException {

	private static final long serialVersionUID = 5230615489167778810L;

	/**
	 * Constructor for semantic errors.
	 *
	 * @param constraintViolations - the list of constraints violated.
	 */
	public SemanticErrorException(final Set<? extends ConstraintViolation<Object>> constraintViolations) {
		super(constraintViolations);
	}
}
