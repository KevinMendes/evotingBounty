/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.bean;

import java.security.PublicKey;
import java.security.cert.Certificate;

/**
 * Implementation of class {@link java.security.cert.Certificate} to be used for testing purposes.
 */
public class TestCertificate extends Certificate {
	private static final long serialVersionUID = -8278774001493088558L;

	private PublicKey publicKey;

	protected TestCertificate(final String type) {

		super(type);
	}

	/**
	 * Constructor.
	 *
	 * @param type      the type of the certificate.
	 * @param publicKey the public key of the certificate.
	 */
	public TestCertificate(final String type, final PublicKey publicKey) {

		super(type);

		this.publicKey = publicKey;
	}

	@Override
	public byte[] getEncoded() {

		return publicKey.getEncoded();
	}

	@Override
	public PublicKey getPublicKey() {

		return publicKey;
	}

	@Override
	public void verify(final PublicKey key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void verify(final PublicKey key, final String sigProvider) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {

		return TestCertificate.class.getSimpleName();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TestCertificate other = (TestCertificate) obj;
		if (publicKey == null) {
			return other.publicKey == null;
		} else {
			return publicKey.equals(other.publicKey);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		return result;
	}
}
