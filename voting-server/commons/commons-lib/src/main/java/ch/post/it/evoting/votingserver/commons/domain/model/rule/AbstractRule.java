/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.rule;

import ch.post.it.evoting.domain.election.validation.ValidationError;

/**
 * Abstract definition of a rule.
 *
 * @param <T> - the generic type of the object which is subject to be validated by the rule.
 */
public interface AbstractRule<T> {

	/**
	 * Applies a rule on the given object.
	 *
	 * @param object - the object to which to apply the rule.
	 * @return A ValidationErrror containing information about the execution of the rule.
	 */
	ValidationError execute(T object);

	/**
	 * Returns the current value of the field name.
	 *
	 * @return Returns the name.
	 */
	String getName();
}
