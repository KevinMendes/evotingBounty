/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.token;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import iaik.pkcs.pkcs11.TokenException;

public class TestCallable implements Callable<Void> {
	private final AtomicInteger _callsToSucceed;

	public TestCallable() {
		this(1);
	}

	public TestCallable(final int callsToSucceed) {
		_callsToSucceed = new AtomicInteger(callsToSucceed);
	}

	@Override
	public Void call() throws Exception {
		final int callsLeft = _callsToSucceed.decrementAndGet();
		if (callsLeft == 0) {
			return null;
		} else {
			throw new TokenException("Still left " + callsLeft + " to succeed");
		}
	}

}
