/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.bean.extensions;

/**
 * Base class for the X509 certificate extensions.
 */
public abstract class AbstractCertificateExtension {

	/**
	 * Returns the extension type of the certificate.
	 *
	 * @return the extension type.
	 */
	public abstract ExtensionType getExtensionType();

	/**
	 * Indicates whether extension is critical.
	 *
	 * @return true if the extension is critical and false otherwise.
	 */
	public abstract boolean isCritical();

	@Override
	public boolean equals(final Object obj) {

		if (obj instanceof AbstractCertificateExtension) {
			final AbstractCertificateExtension certExtensionObj = (AbstractCertificateExtension) obj;

			return getExtensionType() == certExtensionObj.getExtensionType();
		}

		return false;
	}

	@Override
	public int hashCode() {

		return getExtensionType().hashCode();
	}
}
