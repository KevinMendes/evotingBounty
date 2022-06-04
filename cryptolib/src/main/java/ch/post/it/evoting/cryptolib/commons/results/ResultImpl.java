/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.results;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * A basic implementation of a string-based operation outcome which uses a set to store issues.
 */
class ResultImpl implements Result {

	private final Collection<String> issues;
	private final boolean isOk;

	ResultImpl(final String... issues) {
		isOk = issues.length == 0;
		this.issues = new HashSet<>();
		this.issues.addAll(Arrays.asList(issues));
	}

	@Override
	public boolean isOk() {
		return isOk;
	}

	@Override
	public Collection<String> getIssues() {
		return Collections.unmodifiableCollection(issues);
	}
}
