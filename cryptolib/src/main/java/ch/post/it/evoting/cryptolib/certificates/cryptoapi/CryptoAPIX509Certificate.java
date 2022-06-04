/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.cryptoapi;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.commons.results.Result;

/**
 * Interface which provides methods to access X509 certificate data.
 */
public interface CryptoAPIX509Certificate {

	BigInteger getSerialNumber();

	Date getNotBefore();

	Date getNotAfter();

	/**
	 * Checks whether the certificate is currently valid.
	 *
	 * @return the result of validating the certificate.
	 */
	Result checkValidity();

	/**
	 * Checks whether the specified date is within the certificate's period of validity.
	 *
	 * @param date date to check.
	 * @return true is the date is within certificate's period of validity and false otherwise.
	 * @throws GeneralCryptoLibException if date is null
	 */
	boolean checkValidity(final Date date) throws GeneralCryptoLibException;

	X509DistinguishedName getSubjectDn();

	X509DistinguishedName getIssuerDn();

	PublicKey getPublicKey();

	/**
	 * Verifies the certificate's signature with the issuer's public key.
	 *
	 * @param issuerPublicKey issuer public key.
	 * @return true if certificate was successfully verified and false otherwise.
	 * @throws GeneralCryptoLibException if the issuer public key is null or contains null or empty content.
	 */
	boolean verify(final PublicKey issuerPublicKey) throws GeneralCryptoLibException;

	/**
	 * Checks if the certificate is of a specified type, encapsulated in an object of type {@link X509CertificateType}.
	 *
	 * @param certificateType the certificate type, encapsulated in an {@link X509CertificateType} object.
	 * @return true if the certificate is of the specified type and false otherwise.
	 * @see X509CertificateType
	 */
	boolean isCertificateType(final X509CertificateType certificateType);

	/**
	 * Retrieves the certificate in DER format.
	 *
	 * @return the certificate in DER format.
	 */
	byte[] getEncoded();

	/**
	 * Retrieves the certificate in PEM format.
	 *
	 * @return the certificate in PEM format.
	 */
	byte[] getPemEncoded();

	/**
	 * Retrieves the {@link java.security.cert.X509Certificate} object encapsulated by the certificate.
	 *
	 * @return the encapsulated {@link java.security.cert.X509Certificate} object.
	 */
	X509Certificate getCertificate();
}
