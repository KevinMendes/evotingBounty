/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.bean;

import java.util.Arrays;

import javax.crypto.SecretKey;

/**
 * Implementation of interface {@link javax.crypto.SecretKey} to be used for testing purposes.
 */
public class TestSecretKey implements SecretKey {

	private static final long serialVersionUID = 1L;

	private final byte[] keyBytes;

	public TestSecretKey(final byte[] keyBytes) {

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

		return "TestSecretKey Algorithm";
	}

	@Override
	public String getFormat() {

		return "TestSecretKey Format";
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
		final TestSecretKey other = (TestSecretKey) obj;
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
