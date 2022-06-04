/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collection;

import org.bouncycastle.cms.CMSSignedDataStreamGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;

/**
 * Service for working with PKCS7 signed content.
 * <p>
 * This service is introduced to simplify working with PKCS7 programming model provided by Bouncy Castle and to control the used cryptography
 * algorithms by applying the current {@code cryptolibPolicy.propeties}.
 * <p>
 * Implementation must be thread-safe.
 */
public interface CMSService {

	/**
	 * Creates a new {@link CMSSignedDataStreamGenerator} instance for given private key and certificate. The supplied certificate is automatically
	 * added to certificates of {@link CMSSignedDataStreamGenerator}.
	 *
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(PrivateKey key, X509Certificate certificate) throws GeneralSecurityException;

	/**
	 * Creates a new {@link CMSSignedDataStreamGenerator} instance for given private key and certificates. All the certificates are automatically
	 * added to certificates of {@link CMSSignedDataStreamGenerator}.
	 *
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates other certificates
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(PrivateKey key, X509Certificate signerCertificate,
			Collection<X509Certificate> otherCertificates) throws GeneralSecurityException;

	/**
	 * Creates a new {@link CMSSignedDataStreamGenerator} instance for given private key and certificates. All the certificates are automatically
	 * added to certificates of {@link CMSSignedDataStreamGenerator}.
	 *
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates other certificates
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(PrivateKey key, X509Certificate signerCertificate,
			X509Certificate... otherCertificates) throws GeneralSecurityException;

	/**
	 * Creates a new {@link CMSSignedDataStreamGenerator} instance for a given private key entry. The first certificate in {@link
	 * PrivateKeyEntry#getCertificateChain()} is used as a signer certificate. All the certificates must be {@link X509Certificate} and are
	 * automatically added to certificates of {@link CMSSignedDataStreamGenerator}.
	 *
	 * @param entry the entry
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	CMSSignedDataStreamGenerator newCMSSignedDataStreamGenerator(PrivateKeyEntry entry) throws GeneralSecurityException;

	/**
	 * Creates a new {@link SignerInfoGenerator} instance for given private key and certificate. This is a shortcut for {@code
	 * newSignerInfoGeneratorBuilder().setProvateKey(key).setCertificate(certificate).build()} .
	 *
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	SignerInfoGenerator newSignedInfoGenerator(PrivateKey key, X509Certificate certificate) throws GeneralSecurityException;

	/**
	 * Creates a new {@link SignerInfoGenerator} instance for given private key entry. This is a shortcut for {@code
	 * newSignerInfoGenerator(entry.getPrivateKey(), (X509Certificate) entry.getCertificate())} .
	 *
	 * @param entry       the entry
	 * @param certificate the certificate
	 * @return the instance
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	SignerInfoGenerator newSignedInfoGenerator(PrivateKeyEntry entry) throws GeneralSecurityException;

	/**
	 * Creates a {@link SignerInfoGeneratorBuilder} instance.
	 *
	 * @return the instance.
	 */
	SignerInfoGeneratorBuilder newSignerInfoGeneratorBuilder();

	/**
	 * Signs given data using the specified key and certificate.
	 *
	 * @param data        the data
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the signature
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(byte[] data, PrivateKey key, X509Certificate certificate) throws GeneralSecurityException;

	/**
	 * Signs given data using the specified key and certificates.
	 *
	 * @param data              the data
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(byte[] data, PrivateKey key, X509Certificate signerCertificate, Collection<X509Certificate> otherCertificates)
			throws GeneralSecurityException;

	/**
	 * Signs given data using the specified key and certificates.
	 *
	 * @param data              the data
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(byte[] data, PrivateKey key, X509Certificate signerCertificate, X509Certificate... otherCertificates) throws GeneralSecurityException;

	/**
	 * Signs given data using a given private key entry. The first certificate in {@link PrivateKeyEntry#getCertificateChain()} is used as a signer
	 * certificate. All the certificates must be {@link X509Certificate}.
	 *
	 * @param stream the stream
	 * @param entry  the entry
	 * @return the signature
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(byte[] data, PrivateKeyEntry entry) throws GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificate.
	 *
	 * @param file        the file
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(File file, PrivateKey key, X509Certificate certificate) throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificates.
	 *
	 * @param file              the file
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(File file, PrivateKey key, X509Certificate signerCertificate, Collection<X509Certificate> otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificates.
	 *
	 * @param file              the file
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(File file, PrivateKey key, X509Certificate signerCertificate, X509Certificate... otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using a given private key entry. The first certificate in {@link PrivateKeyEntry#getCertificateChain()} is
	 * used as a signer certificate. All the certificates must be {@link X509Certificate}.
	 *
	 * @param file  the file
	 * @param entry the entry
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(File file, PrivateKeyEntry entry) throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given stream using the specified key and certificate. The client is responsible for closing the stream.
	 *
	 * @param stream      the stream
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(InputStream stream, PrivateKey key, X509Certificate certificate) throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given stream using the specified key and certificates. The client is responsible for closing the stream.
	 *
	 * @param stream            the stream
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred.
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(InputStream stream, PrivateKey key, X509Certificate signerCertificate, Collection<X509Certificate> otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given stream using the specified key and certificates. The client is responsible for closing the stream.
	 *
	 * @param stream            the stream
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(InputStream stream, PrivateKey key, X509Certificate signerCertificate, X509Certificate... otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given stream using a given private key entry. The first certificate in {@link PrivateKeyEntry#getCertificateChain()} is
	 * used as a signer certificate. All the certificates must be {@link X509Certificate}. The client is responsible for closing the stream.
	 *
	 * @param stream the stream
	 * @param entry  the entry
	 * @return the signature
	 * @throws IOException                  I/O error occurred.
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(InputStream stream, PrivateKeyEntry entry) throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificate.
	 *
	 * @param file        the file
	 * @param key         the key
	 * @param certificate the certificate
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(Path file, PrivateKey key, X509Certificate certificate) throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificates.
	 *
	 * @param file              the file
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(Path file, PrivateKey key, X509Certificate signerCertificate, Collection<X509Certificate> otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using the specified key and certificates.
	 *
	 * @param file              the file
	 * @param key               the key
	 * @param signerCertificate the signer certificate
	 * @param otherCertificates the other certificates
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(Path file, PrivateKey key, X509Certificate signerCertificate, X509Certificate... otherCertificates)
			throws IOException, GeneralSecurityException;

	/**
	 * Signs the data from a given file using a given private key entry. The first certificate in {@link PrivateKeyEntry#getCertificateChain()} is
	 * used as a signer certificate. All the certificates must be {@link X509Certificate}.
	 *
	 * @param file  the file
	 * @param entry the entry
	 * @return the signature
	 * @throws IOException                  I/O error occurred
	 * @throws InvalidKeyException          the key is invalid
	 * @throws CertificateEncodingException the certificate is invalid
	 * @throws GeneralSecurityException     failed to create generator.
	 */
	byte[] sign(Path file, PrivateKeyEntry entry) throws IOException, GeneralSecurityException;
}
