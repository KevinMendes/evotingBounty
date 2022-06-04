/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;

public class CertificateTools {

	private static final int DIGITAL_SIGNATURE = 0;

	private static final int NON_REPUDIATION = 1;

	private static final int KEY_ENCIPHERMENT = 2;

	private static final int DATA_ENCIPHERMENT = 3;

	private static final int KEY_CERT_SIGN = 5;

	private static final int CRL_SIGN = 6;

	private CertificateTools() {
		// Intentionally left blank.
	}

	/**
	 * Extract the type (also known as "key usage") from an {@link X509Certificate}.
	 *
	 * @param certificate the certificate from which to extract its type.
	 * @return the type.
	 */
	public static X509CertificateType extractTypeFromCertificate(final X509Certificate certificate) throws CryptographicOperationException {

		final boolean[] keyUsage = certificate.getKeyUsage();

		if (isSigningCertificate(keyUsage)) {
			return X509CertificateType.SIGN;
		}
		if (isEncryptionCertificate(keyUsage)) {
			return X509CertificateType.ENCRYPT;
		}
		if (isCACertificate(keyUsage)) {
			return X509CertificateType.CERTIFICATE_AUTHORITY;
		} else {
			throw new CryptographicOperationException("Unexpected certificate type");
		}
	}

	private static boolean isSigningCertificate(final boolean[] keyUsage) {

		return keyUsage[DIGITAL_SIGNATURE] && keyUsage[NON_REPUDIATION];
	}

	private static boolean isEncryptionCertificate(final boolean[] keyUsage) {

		return keyUsage[KEY_ENCIPHERMENT] && keyUsage[DATA_ENCIPHERMENT];
	}

	private static boolean isCACertificate(final boolean[] keyUsage) {

		return keyUsage[KEY_CERT_SIGN] && keyUsage[CRL_SIGN];
	}

	/**
	 * Adapter method that creates a {@link ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate} from a {@link Certificate}.
	 * <p>
	 * Note: this method assumes that the received {@link Certificate} is an {@link java.security.cert.X509Certificate}.
	 *
	 * @param certificateToValidate the Certificate from which to create a CryptoX509Certificate.
	 * @return a new CryptoX509Certificate.
	 */
	public static CryptoX509Certificate getCryptoX509Certificate(final Certificate certificateToValidate) throws CryptographicOperationException {

		X509Certificate x509Certificate = (X509Certificate) certificateToValidate;

		CryptoX509Certificate cryptoX509Certificate;
		try {
			cryptoX509Certificate = new CryptoX509Certificate(x509Certificate);
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured while trying to create a CryptoX509Certificate from the received certificate",
					e);
		}

		return cryptoX509Certificate;
	}
}
