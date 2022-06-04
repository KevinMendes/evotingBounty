/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;

/**
 * Basic constraints certificate extension.
 */
public class BasicConstraintsExtension extends AbstractCertificateExtension {

	private final boolean isCritical;

	private final boolean isCertificateAuthority;

	/**
	 * Creates a certificate extension with provided arguments.
	 *
	 * @param isCritical             Flag indicating whether extension is critical.
	 * @param isCertificateAuthority Flag indicating whether certificate is a certificate authority.
	 */
	public BasicConstraintsExtension(final boolean isCritical, final boolean isCertificateAuthority) {

		super();

		this.isCritical = isCritical;
		this.isCertificateAuthority = isCertificateAuthority;
	}

	/**
	 * Constructor, with {@code isCritical} flag set to default value defined in {@link X509CertificateConstants#CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT}
	 * .
	 *
	 * @param isCertificateAuthority flag indicating whether certificate is a certificate authority.
	 */
	public BasicConstraintsExtension(final boolean isCertificateAuthority) {

		super();

		isCritical = X509CertificateConstants.CERTIFICATE_EXTENSION_IS_CRITICAL_FLAG_DEFAULT;
		this.isCertificateAuthority = isCertificateAuthority;
	}

	@Override
	public ExtensionType getExtensionType() {

		return ExtensionType.BASIC_CONSTRAINTS;
	}

	@Override
	public boolean isCritical() {

		return isCritical;
	}

	public boolean isCertificateAuthority() {

		return isCertificateAuthority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isCertificateAuthority ? 1231 : 1237);
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

		final BasicConstraintsExtension other = (BasicConstraintsExtension) obj;
		if (isCertificateAuthority != other.isCertificateAuthority) {
			return false;
		}

		return isCritical == other.isCritical;
	}
}
