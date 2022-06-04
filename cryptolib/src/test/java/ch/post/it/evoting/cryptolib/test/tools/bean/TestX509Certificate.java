/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.test.tools.bean;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

/**
 * Implementation of class {@link java.security.cert.X509Certificate} to be used for testing purposes.
 */
public class TestX509Certificate extends X509Certificate {
	private static final long serialVersionUID = -5494125476368422902L;

	private final PublicKey publicKey;

	private final Date notBefore;

	private final Date notAfter;

	public TestX509Certificate(final PublicKey publicKey) {

		this.publicKey = publicKey;

		notBefore = new Date(System.currentTimeMillis());

		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		notAfter = calendar.getTime();
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
	public Date getNotBefore() {

		return new Date(notBefore.getTime());
	}

	@Override
	public Date getNotAfter() {

		return new Date(notAfter.getTime());
	}

	@Override
	public boolean hasUnsupportedCriticalExtension() {

		return false;
	}

	@Override
	public Set<String> getCriticalExtensionOIDs() {

		return new HashSet<>();
	}

	@Override
	public Set<String> getNonCriticalExtensionOIDs() {

		return new HashSet<>();
	}

	@Override
	public byte[] getExtensionValue(final String oid) {

		return new byte[0];
	}

	@Override
	public void checkValidity() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void checkValidity(final Date date) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getVersion() {

		return 0;
	}

	@Override
	public BigInteger getSerialNumber() {

		return BigInteger.ZERO;
	}

	@Override
	public Principal getIssuerDN() {

		return new X500Principal("TestX509Certificate Issuer DN");
	}

	@Override
	public Principal getSubjectDN() {

		return new X500Principal("TestX509Certificate Subject DN");
	}

	@Override
	public byte[] getTBSCertificate() {

		return new byte[0];
	}

	@Override
	public byte[] getSignature() {

		return new byte[0];
	}

	@Override
	public String getSigAlgName() {

		return "Test Signature Algorithm Name";
	}

	@Override
	public String getSigAlgOID() {
		return "Test Signature Algorithm OID";
	}

	@Override
	public byte[] getSigAlgParams() {

		return new byte[0];
	}

	@Override
	public boolean[] getIssuerUniqueID() {

		return new boolean[0];
	}

	@Override
	public boolean[] getSubjectUniqueID() {

		return new boolean[0];
	}

	@Override
	public boolean[] getKeyUsage() {

		return new boolean[0];
	}

	@Override
	public int getBasicConstraints() {

		return 0;
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

		return TestX509Certificate.class.getSimpleName();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
		final TestX509Certificate other = (TestX509Certificate) obj;
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
