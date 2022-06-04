/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.bean;

import java.security.PublicKey;
import java.util.Arrays;

/**
 * Implementation of interface {@link java.security.PublicKey} to be used for testing purposes.
 */
public class TestPublicKey implements PublicKey {

	private static final long serialVersionUID = 1L;

	private final byte[] keyBytes;

	public TestPublicKey(final byte[] keyBytes) {

		if (keyBytes != null) {
			this.keyBytes = keyBytes.clone();
		} else {
			this.keyBytes = null;
		}
	}

	@Override
	public byte[] getEncoded() {

		if (keyBytes != null) {
			return keyBytes.clone();
		} else {
			return null;
		}
	}

	@Override
	public String getAlgorithm() {

		return "TestPublicKey Algorithm";
	}

	@Override
	public String getFormat() {

		return "TestPublicKey Format";
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
		final TestPublicKey other = (TestPublicKey) obj;
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
