/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.cryptolib.policy;

import java.util.Properties;

public interface AbstractPolicyFactory<T extends AbstractPolicy> {

	T getDefaultPolicy();

	T getPolicy(Properties properties);
}
