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
public class SyntaxErrorException extends ValidationException {

	private static final long serialVersionUID = 4244613851938334104L;

	/**
	 * Constructor for syntax errors.
	 *
	 * @param <T>                  the type of constraint violation.
	 * @param constraintViolations - the list of constraints violated.
	 */
	public SyntaxErrorException(final Set<? extends ConstraintViolation<Object>> constraintViolations) {
		super(constraintViolations);
	}
}
