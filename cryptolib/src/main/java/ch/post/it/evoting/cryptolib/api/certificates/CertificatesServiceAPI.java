/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.certificates;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;

/**
 * This interface defines public operations that can be done with certificates.
 */
public interface CertificatesServiceAPI {

	/**
	 * Creates a root authority X509 certificate with its corresponding extensions.
	 *
	 * @param rootCertificateData contains all the necessary certificate data: subject, validity dates and public key.
	 * @param rootPrivateKey      the private key.
	 * @return the authority certificate.
	 * @throws GeneralCryptoLibException if {@code rootCertificateData} is invalid or {@code rootPrivateKey} cannot be parsed.
	 */
	CryptoAPIX509Certificate createRootAuthorityX509Certificate(final RootCertificateData rootCertificateData, final PrivateKey rootPrivateKey)
			throws GeneralCryptoLibException;

	/**
	 * Creates an intermediate authority X509 certificate with its corresponding extensions.
	 *
	 * @param certificateData  contains all the necessary certificate data: issuer, subject, validity dates and public key.
	 * @param issuerPrivateKey the private key.
	 * @return the authority certificate.
	 * @throws GeneralCryptoLibException if {@code certificateData} is invalid or {@code issuerPrivateKey} cannot be parsed.
	 */
	CryptoAPIX509Certificate createIntermediateAuthorityX509Certificate(final CertificateData certificateData, final PrivateKey issuerPrivateKey)
			throws GeneralCryptoLibException;

	/**
	 * Creates an signing X509 certificate with its corresponding extensions.
	 *
	 * @param certificateData  contains all the necessary certificate data: issuer, subject, validity dates and public key.
	 * @param issuerPrivateKey the private key.
	 * @return the sign certificate.
	 * @throws GeneralCryptoLibException if {@code certificateData} is invalid or {@code issuerPrivateKey} cannot be parsed.
	 */
	CryptoAPIX509Certificate createSignX509Certificate(final CertificateData certificateData, final PrivateKey issuerPrivateKey)
			throws GeneralCryptoLibException;

	/**
	 * Create an encryption X509 certificate with its corresponding extensions.
	 *
	 * @param certificateData  contains all the necessary certificate data: issuer, subject, validity Dates and public key.
	 * @param issuerPrivateKey the private key.
	 * @return the encryption certificate.
	 * @throws GeneralCryptoLibException if {@code certificateData} is invalid or {@code issuerPrivateKey} cannot be parsed.
	 */
	CryptoAPIX509Certificate createEncryptionX509Certificate(final CertificateData certificateData, final PrivateKey issuerPrivateKey)
			throws GeneralCryptoLibException;

	PKCS10CertificationRequest generate(final PublicKey publickey, final PrivateKey privatekey, final X500Principal subject)
			throws GeneralCryptoLibException;
}
