/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean;

import java.util.EnumSet;

import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.BasicConstraintsExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsage;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsageExtension;

/**
 * Enum that defines the supported certificate types.
 */
public enum X509CertificateType {

	/**
	 * Certificate authority certificate type.
	 *
	 * <ul>
	 *   <li>Basic Constraints extension with the cA flag set to TRUE
	 *   <li>Certificate signing key usage extension
	 *   <li>CRL signing key usage extension
	 * </ul>
	 */
	CERTIFICATE_AUTHORITY(true, EnumSet.of(CertificateKeyUsage.KEY_CERT_SIGN, CertificateKeyUsage.CRL_SIGN)),

	/**
	 * Sign certificate type.
	 *
	 * <ul>
	 *   <li>Digital signature key usage extension
	 *   <li>Non-repudiation key usage extension
	 * </ul>
	 */
	SIGN(false, EnumSet.of(CertificateKeyUsage.DIGITAL_SIGNATURE, CertificateKeyUsage.NON_REPUDIATION)),

	/**
	 * Encrypt certificate type.
	 *
	 * <ul>
	 *   <li>Key encipherment key usage extension
	 *   <li>Data encipherment key usage extension
	 * </ul>
	 */
	ENCRYPT(false, EnumSet.of(CertificateKeyUsage.KEY_ENCIPHERMENT, CertificateKeyUsage.DATA_ENCIPHERMENT));

	private final EnumSet<CertificateKeyUsage> certificateKeyUsages;

	private final boolean isCertAuthority;

	X509CertificateType(final boolean isCertAuthority, final EnumSet<CertificateKeyUsage> certificateKeyUsage) {

		this.isCertAuthority = isCertAuthority;
		this.certificateKeyUsages = certificateKeyUsage;
	}

	public AbstractCertificateExtension[] getExtensions() {

		return new AbstractCertificateExtension[] { new BasicConstraintsExtension(isCertAuthority),
				new CertificateKeyUsageExtension(certificateKeyUsages) };
	}
}
