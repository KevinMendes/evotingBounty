/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.util.encoders.DecoderException;

import ch.post.it.evoting.cryptolib.CryptolibInitializer;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Provides utilities for writing cryptographic primitives to and reading them from PEM format.
 */
public final class PemUtils {

	private static final String PEM_HEADER_PREFIX = "-----BEGIN";

	private static final String PEM_FOOTER_PREFIX = "-----END";

	private static final String PUBLIC_KEY_PEM_HEADER_SUFFIX = "PUBLIC KEY-----";

	private static final String PRIVATE_KEY_PEM_HEADER_SUFFIX = "PRIVATE KEY-----";

	private static final String CERTIFICATE_PEM_HEADER_SUFFIX = "CERTIFICATE-----";

	private static final String CERTIFICATE_REQUEST_PEM_HEADER_SUFFIX = "CERTIFICATE REQUEST-----";

	static {
		CryptolibInitializer.initialize();
	}

	private PemUtils() {
	}

	/**
	 * Converts a {@link java.security.PublicKey} to PEM format.
	 *
	 * @param key the {@link java.security.PublicKey} to convert.
	 * @return the {@link java.security.PublicKey} in PEM format.
	 * @throws GeneralCryptoLibException if the input validation or the object to PEM conversion fails.
	 */
	public static String publicKeyToPem(final PublicKey key) throws GeneralCryptoLibException {

		Validate.notNull(key, "Public key");
		Validate.notNullOrEmpty(key.getEncoded(), "Public key content");

		return toPem(key, PublicKey.class);
	}

	/**
	 * Converts a {@link java.security.PrivateKey} to PEM format.
	 *
	 * @param key the {@link java.security.PrivateKey} to convert.
	 * @return the {@link java.security.PrivateKey} in PEM format.
	 * @throws GeneralCryptoLibException if the input validation or the object to PEM conversion fails.
	 */
	public static String privateKeyToPem(final PrivateKey key) throws GeneralCryptoLibException {

		Validate.notNull(key, "Private key");
		Validate.notNullOrEmpty(key.getEncoded(), "Private key content");

		return toPem(key, PrivateKey.class);
	}

	/**
	 * Converts a {@link java.security.cert.Certificate} to PEM format.
	 *
	 * @param certificate the {@link java.security.cert.Certificate} to convert.
	 * @return the {@link java.security.cert.Certificate} in PEM format.
	 * @throws GeneralCryptoLibException if the input validation or the object to PEM conversion fails.
	 */
	public static String certificateToPem(final Certificate certificate) throws GeneralCryptoLibException {

		Validate.notNull(certificate, "Certificate");

		return toPem(certificate, Certificate.class);
	}

	/**
	 * Converts a {@link org.bouncycastle.pkcs.PKCS10CertificationRequest} to PEM format.
	 *
	 * @param csr the {@link org.bouncycastle.pkcs.PKCS10CertificationRequest} to convert.
	 * @return the {@link org.bouncycastle.pkcs.PKCS10CertificationRequest} in PEM format.
	 * @throws GeneralCryptoLibException if the input validation or the object to PEM conversion fails.
	 */
	public static String certificateSigningRequestToPem(final PKCS10CertificationRequest csr) throws GeneralCryptoLibException {

		Validate.notNull(csr, "Certificate signing request");

		return toPem(csr, PKCS10CertificationRequest.class);
	}

	/**
	 * Retrieves a {@link java.security.PublicKey} from a string in PEM format.
	 *
	 * @param pemStr the {@link java.security.PublicKey} as a string in PEM format.
	 * @return the {@link java.security.PublicKey}
	 * @throws GeneralCryptoLibException if the input validation or the PEM to object conversion fails.
	 */
	public static PublicKey publicKeyFromPem(final String pemStr) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(pemStr, "Public key PEM string");
		validatePemFormat(pemStr, PUBLIC_KEY_PEM_HEADER_SUFFIX, "Public key PEM string");

		final SubjectPublicKeyInfo publicKeyInfo = fromPem(formatPemString(pemStr, PUBLIC_KEY_PEM_HEADER_SUFFIX), SubjectPublicKeyInfo.class);

		try {
			return new JcaPEMKeyConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getPublicKey(publicKeyInfo);
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("Could not retrieve public key from object of type {@link " + SubjectPublicKeyInfo.class + "}", e);
		}
	}

	/**
	 * Retrieves a {@link java.security.PrivateKey} from a string in PEM format.
	 *
	 * @param pemStr the {@link java.security.PrivateKey} as a string in PEM format.
	 * @return the {@link java.security.PrivateKey}
	 * @throws GeneralCryptoLibException if the input validation or the PEM to object conversion fails.
	 */
	public static PrivateKey privateKeyFromPem(final String pemStr) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(pemStr, "Private key PEM string");
		validatePemFormat(pemStr, PRIVATE_KEY_PEM_HEADER_SUFFIX, "Private key PEM string");

		final PEMKeyPair pemKeyPair = fromPem(formatPemString(pemStr, PRIVATE_KEY_PEM_HEADER_SUFFIX), PEMKeyPair.class);

		try {
			return new JcaPEMKeyConverter().getPrivateKey(pemKeyPair.getPrivateKeyInfo());
		} catch (final PEMException e) {
			throw new GeneralCryptoLibException("Could not retrieve private key from object of type {@link " + PEMKeyPair.class + "}", e);
		}
	}

	/**
	 * Retrieves a {java.security.cert.Certificate} from a string in PEM format.
	 *
	 * @param pemStr the {java.security.cert.Certificate} as a string in PEM format.
	 * @return the {java.security.cert.Certificate}
	 * @throws GeneralCryptoLibException if the input validation or the PEM to object conversion fails.
	 */
	public static Certificate certificateFromPem(final String pemStr) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(pemStr, "Certificate PEM string");
		validatePemFormat(pemStr, CERTIFICATE_PEM_HEADER_SUFFIX, "Certificate PEM string");

		final X509CertificateHolder certificateHolder = fromPem(formatPemString(pemStr, CERTIFICATE_PEM_HEADER_SUFFIX), X509CertificateHolder.class);

		try {
			return new JcaX509CertificateConverter().getCertificate(certificateHolder);
		} catch (final CertificateException e) {
			throw new GeneralCryptoLibException("Could not retrieve public key from object of type {@link " + X509CertificateHolder.class + "}", e);
		}
	}

	public static void saveCertificateToFile(final CryptoAPIX509Certificate certificate, final Path path) throws GeneralCryptoLibException {
		Validate.notNull(certificate, "certificate");
		Validate.notNull(path, "path");

		try (final PrintWriter writer = new PrintWriter(path.toFile())) {
			writer.write(new String(certificate.getPemEncoded(), StandardCharsets.UTF_8));
		} catch (final FileNotFoundException e) {
			throw new GeneralCryptoLibException("An error occurred while persisting the certificate " + path, e);
		}
	}

	/**
	 * Converts a PEM string to a {@link org.bouncycastle.pkcs.PKCS10CertificationRequest}.
	 *
	 * @param pemStr the PEM string to convert.
	 * @return the {@link org.bouncycastle.pkcs.PKCS10CertificationRequest}.
	 * @throws GeneralCryptoLibException if the input validation or conversion from PEM to {@link org.bouncycastle.pkcs.PKCS10CertificationRequest}
	 *                                   fails.
	 */
	public static JcaPKCS10CertificationRequest certificateSigningRequestFromPem(final String pemStr) throws GeneralCryptoLibException {

		Validate.notNullOrBlank(pemStr, "Certificate signing request PEM string");
		validatePemFormat(pemStr, CERTIFICATE_REQUEST_PEM_HEADER_SUFFIX, "Certificate signing request PEM string");

		final PKCS10CertificationRequest certificateRequestHolder = fromPem(formatPemString(pemStr, CERTIFICATE_REQUEST_PEM_HEADER_SUFFIX),
				PKCS10CertificationRequest.class);

		return new JcaPKCS10CertificationRequest(certificateRequestHolder);
	}

	private static <T> String toPem(final Object object, final Class<T> objectClass) throws GeneralCryptoLibException {

		final StringWriter pemStringWriter = new StringWriter();
		try (final JcaPEMWriter writer = new JcaPEMWriter(pemStringWriter)) {
			writer.writeObject(object);
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("Could not convert object of type {@link " + objectClass + "} to PEM format.", e);
		}

		return pemStringWriter.toString();
	}

	private static <T> T fromPem(final String pemStr, final Class<T> objectClass) throws GeneralCryptoLibException {

		try (final PEMParser parser = new PEMParser(new StringReader(pemStr))) {
			final Object object = parser.readObject();

			return objectClass.cast(object);
		} catch (final IOException | DecoderException e) {
			throw new GeneralCryptoLibException("Could not convert PEM string " + pemStr + " to object of type {@link " + objectClass + "}", e);
		}
	}

	private static void validatePemFormat(final String pemStr, final String headerSuffix, final String label) throws GeneralCryptoLibException {

		if (!pemStr.contains(PEM_HEADER_PREFIX) || !pemStr.contains(headerSuffix)) {
			throw new GeneralCryptoLibException(label + " does not contain valid header.");
		}

		if (!pemStr.contains(PEM_FOOTER_PREFIX)) {
			throw new GeneralCryptoLibException(label + " does not contain valid footer.");
		}
	}

	private static String formatPemString(final String pemStr, final String headerSuffix) {

		String formattedPemStr = pemStr.replace("\n", "").replace("\r", "");

		formattedPemStr = formattedPemStr.replaceFirst(headerSuffix, headerSuffix + "\n");

		formattedPemStr = formattedPemStr.replaceFirst(PEM_FOOTER_PREFIX, "\n" + PEM_FOOTER_PREFIX);

		return formattedPemStr;
	}
}
