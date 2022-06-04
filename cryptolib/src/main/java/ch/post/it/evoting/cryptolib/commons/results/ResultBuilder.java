/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.results;

import java.util.HashSet;
import java.util.Set;

/**
 * A factory the produces {@link Result}s following the Builder pattern. This class is thread-safe.
 */
public class ResultBuilder {

	/**
	 * A set so that there are no duplicated issues.
	 */
	private final Set<String> issues = new HashSet<>();

	/**
	 * Creates a result for a failed operation.
	 *
	 * @param issues The issues that arose during the operation.
	 */
	public static Result failed(final String... issues) {
		return new ResultImpl(issues);
	}

	/**
	 * @return A result for a successful operation.
	 */
	public static Result ok() {
		return new ResultImpl();
	}

	/**
	 * Adds an issue to the result. This method call is synchronized so that adding issues from a thread does not result in concurrency errors.
	 *
	 * @param issue the issue to add
	 * @return the builder, for fluency.
	 */
	public synchronized ResultBuilder add(final String issue) {
		issues.add(issue);

		return this;
	}

	/**
	 * Adds the result of another operation to this one. This method call is synchronized so that adding results from a thread does not end up in
	 * concurrency errors.
	 *
	 * @param result the result to add
	 * @return the builder, for fluency.
	 */
	public synchronized ResultBuilder add(final OperationOutcome<String> result) {
		issues.addAll(result.getIssues());

		return this;
	}

	/**
	 * @return the validation result.
	 */
	public Result build() {
		return issues.isEmpty() ? ok() : failed(issues.toArray(new String[0]));
	}
}
