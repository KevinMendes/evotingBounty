/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.BasicConstraintsExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsage;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsageExtension;

/**
 * Helper class to add extensions to certificates. It does not implement the Builder pattern.
 */
class ExtensionBuilder {

	private static final String ADD_CERTIFICATE_EXTENSION_ERROR_MESSAGE = "Could not add extension to certificate.";

	private final X509v3CertificateBuilder certificateBuilder;

	/**
	 * Creates an extension to add to the certificate builder defined in argument.
	 *
	 * @param certificateBuilder An {@link X509v3CertificateBuilder} to which extensions are to be added.
	 */
	ExtensionBuilder(final X509v3CertificateBuilder certificateBuilder) {
		this.certificateBuilder = certificateBuilder;
	}

	/**
	 * Generates a key usage mask, based on the specified key usages. The mask is needed for creating a key usage extension object.
	 *
	 * @param certificateKeyUsages key usages for generating mask.
	 * @return key usage mask.
	 */
	private static int generateKeyUsageMask(final EnumSet<CertificateKeyUsage> certificateKeyUsages) {

		int keyUsageMask = 0;

		final Map<CertificateKeyUsage, Integer> keyUsageTranslator = getKeyUsageBouncyCastleTranslator();

		for (final CertificateKeyUsage usage : certificateKeyUsages) {
			keyUsageMask |= keyUsageTranslator.get(usage);
		}

		return keyUsageMask;
	}

	private static Map<CertificateKeyUsage, Integer> getKeyUsageBouncyCastleTranslator() {
		final EnumMap<CertificateKeyUsage, Integer> keyUsageTranslator = new EnumMap<>(CertificateKeyUsage.class);

		keyUsageTranslator.put(CertificateKeyUsage.DIGITAL_SIGNATURE, KeyUsage.digitalSignature);
		keyUsageTranslator.put(CertificateKeyUsage.NON_REPUDIATION, KeyUsage.nonRepudiation);
		keyUsageTranslator.put(CertificateKeyUsage.KEY_ENCIPHERMENT, org.bouncycastle.asn1.x509.KeyUsage.keyEncipherment);
		keyUsageTranslator.put(CertificateKeyUsage.DATA_ENCIPHERMENT, org.bouncycastle.asn1.x509.KeyUsage.dataEncipherment);
		keyUsageTranslator.put(CertificateKeyUsage.KEY_AGREEMENT, org.bouncycastle.asn1.x509.KeyUsage.keyAgreement);
		keyUsageTranslator.put(CertificateKeyUsage.KEY_CERT_SIGN, org.bouncycastle.asn1.x509.KeyUsage.keyCertSign);
		keyUsageTranslator.put(CertificateKeyUsage.CRL_SIGN, org.bouncycastle.asn1.x509.KeyUsage.cRLSign);
		keyUsageTranslator.put(CertificateKeyUsage.ENCIPHER_ONLY, org.bouncycastle.asn1.x509.KeyUsage.encipherOnly);
		keyUsageTranslator.put(CertificateKeyUsage.DECIPHER_ONLY, org.bouncycastle.asn1.x509.KeyUsage.decipherOnly);

		return keyUsageTranslator;
	}

	/**
	 * Adds an {@code extension} to the certificate builder.
	 *
	 * @param extension An {@link AbstractCertificateExtension} to be added to the builder.
	 */
	public void addExtension(final AbstractCertificateExtension extension) {

		try {
			switch (extension.getExtensionType()) {
			case BASIC_CONSTRAINTS:
				final BasicConstraintsExtension basicConstraintsExtension = (BasicConstraintsExtension) extension;

				certificateBuilder.addExtension(Extension.basicConstraints, extension.isCritical(),
						new BasicConstraints(basicConstraintsExtension.isCertificateAuthority()));

				break;
			case KEY_USAGE:
				final CertificateKeyUsageExtension keyUsageExtension = (CertificateKeyUsageExtension) extension;

				certificateBuilder.addExtension(Extension.keyUsage, extension.isCritical(),
						new KeyUsage(generateKeyUsageMask(keyUsageExtension.getKeyUsages())));

				break;
			default:
				break;
			}
		} catch (final CertIOException e) {
			throw new CryptoLibException(ADD_CERTIFICATE_EXTENSION_ERROR_MESSAGE, e);
		}
	}
}
