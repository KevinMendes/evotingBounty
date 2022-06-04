/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.results;

import java.util.Collection;

/**
 * A report of an operation's outcome, with a detail of all relevant issues encountered.
 *
 * @param <T> the type that represents the eventual issues encountered during the operation.
 */
public interface OperationOutcome<T> {

	/**
	 * @return Whether the operation can be considered to have ended successfully.
	 */
	boolean isOk();

	/**
	 * @return A collection of objects representing issues that arose during the operation.
	 */
	Collection<T> getIssues();
}
