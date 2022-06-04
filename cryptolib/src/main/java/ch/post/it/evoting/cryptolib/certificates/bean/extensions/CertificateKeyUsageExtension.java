/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

import java.util.EnumSet;

import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

/**
 * Key usage certificate extension.
 */
public class CertificateKeyUsageExtension extends AbstractCertificateExtension {

	private final boolean isCritical;

	private final EnumSet<CertificateKeyUsage> certificateKeyUsages;

	/**
	 * Creates a certificate extension with provided arguments.
	 *
	 * @param isCritical           flag indicating whether extension is critical.
	 * @param certificateKeyUsages key usages of extension.
	 */
	public CertificateKeyUsageExtension(final boolean isCritical, final EnumSet<CertificateKeyUsage> certificateKeyUsages) {

		super();

		this.isCritical = isCritical;
		this.certificateKeyUsages = certificateKeyUsages.clone();
	}

	/**
	 * Constructor, with {@code isCritical} flag set to default value defined in {@link X509CertificateConstants#CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT}
	 * .
	 *
	 * @param certificateKeyUsages key usages of extension.
	 */
	public CertificateKeyUsageExtension(final EnumSet<CertificateKeyUsage> certificateKeyUsages) {

		super();

		isCritical = X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;
		this.certificateKeyUsages = certificateKeyUsages.clone();
	}

	@Override
	public ExtensionType getExtensionType() {

		return ExtensionType.KEY_USAGE;
	}

	@Override
	public boolean isCritical() {

		return isCritical;
	}

	/**
	 * Returns a list of of the key usages.
	 *
	 * @return a list of of the key usages.
	 */
	public EnumSet<CertificateKeyUsage> getKeyUsages() {

		return certificateKeyUsages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((certificateKeyUsages == null) ? 0 : certificateKeyUsages.hashCode());
		result = prime * result + (isCritical ? 1231 : 1237);
		return result;
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
		final CertificateKeyUsageExtension other = (CertificateKeyUsageExtension) obj;
		if (certificateKeyUsages == null) {
			if (other.certificateKeyUsages != null) {
				return false;
			}
		} else if (!certificateKeyUsages.equals(other.certificateKeyUsages)) {
			return false;
		}
		return isCritical == other.isCritical;
	}
}
