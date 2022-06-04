/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.bean;

import java.security.PrivateKey;
import java.util.Arrays;

/**
 * Implementation of interface {@link java.security.PrivateKey} to be used for testing purposes.
 */
public class TestPrivateKey implements PrivateKey {

	private static final long serialVersionUID = 1L;

	private final byte[] keyBytes;

	public TestPrivateKey(final byte[] keyBytes) {

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

		return "TestPrivateKey Algorithm";
	}

	@Override
	public String getFormat() {

		return "TestPrivateKey Format";
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
		final TestPrivateKey other = (TestPrivateKey) obj;
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
