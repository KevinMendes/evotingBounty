/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.primes.bean;

import java.util.Arrays;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;

/**
 * Implementation of interface {@link CryptoAPIDerivedKey} to be used for testing purposes.
 */
public class TestCryptoDerivedKey implements CryptoAPIDerivedKey {

	private final byte[] keyBytes;

	public TestCryptoDerivedKey(final byte[] keyBytes) {

		this.keyBytes = keyBytes;
	}

	@Override
	public byte[] getEncoded() {

		return keyBytes;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TestCryptoDerivedKey other = (TestCryptoDerivedKey) obj;

		return Arrays.equals(keyBytes, other.keyBytes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keyBytes);
		return result;
	}
}
