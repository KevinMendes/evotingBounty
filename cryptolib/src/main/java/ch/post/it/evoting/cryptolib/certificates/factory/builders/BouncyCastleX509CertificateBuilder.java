/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import java.math.BigInteger;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;

/**
 * Class which implements {@link X509CertificateBuilder} that uses BouncyCastle as the cryptographic service provider.
 */
public class BouncyCastleX509CertificateBuilder implements X509CertificateBuilder {

	private static final String BUILD_CERTIFICATE_ERROR_MESSAGE = "Could not build certificate.";

	private static final String CREATE_CERTIFICATE_SIGNER_ERROR_MESSAGE = "Could not create signer for certificate issuer private key.";

	private final String signatureAlgorithm;

	private final X509DistinguishedName subjectDn;
	private final X509v3CertificateBuilder certBuilder;
	private final Provider provider;
	private X500NameBuilder subjectX509NameBuilder;

	/**
	 * Creates an instance of certificate builder with the provided components.
	 *
	 * @param signatureAlgorithm  algorithm used to sign certificate with issuer private key.
	 * @param provider            name of cryptographic service {@link Provider} of certificates.
	 * @param subjectPublicKey    public key of certificate subject.
	 * @param subjectDn           subject distinguished name of certificate (NOTE: for self-signed certificates this is same as issuer distinguished
	 *                            name).
	 * @param issuerDn            issuer distinguished name of certificate.
	 * @param validityDates       dates of validity of certificate.
	 * @param cryptoIntegerRandom encapsulates a SecureRandom to create the serialNumber.
	 */
	public BouncyCastleX509CertificateBuilder(final String signatureAlgorithm, final Provider provider, final PublicKey subjectPublicKey,
			final X509DistinguishedName subjectDn, final Principal issuerDn, final ValidityDates validityDates,
			final CryptoRandomInteger cryptoIntegerRandom) {

		this.signatureAlgorithm = signatureAlgorithm;
		this.provider = provider;
		this.subjectDn = subjectDn;

		// Create subject public key info instance with subject public key.
		final SubjectPublicKeyInfo subjPubKeyInfo = SubjectPublicKeyInfo.getInstance(subjectPublicKey.getEncoded());

		// Generate the X509 name builders.
		generateSubjectX500Builder();

		// Generate serial number.
		final BigInteger serialNumber;
		try {
			serialNumber = cryptoIntegerRandom.genRandomIntegerByBits(X509CertificateConstants.CERTIFICATE_SERIAL_NUMBER_MAX_BIT_LENGTH);
		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException(e);
		}

		// Create certificate builder instance.
		certBuilder = new X509v3CertificateBuilder(new X500Name(issuerDn.getName()), serialNumber, validityDates.getNotBefore(),
				validityDates.getNotAfter(), subjectX509NameBuilder.build(), subjPubKeyInfo);
	}

	public static Principal getPrincipalIssuerDn(final X509DistinguishedName issuerDn) {
		return new X500Principal(createNameBuilder(issuerDn).build().toString());
	}

	/**
	 * Creates an instance of a name builder from the data in the supplied distinguished name.
	 *
	 * @param distinguishedName the DN used to populate the name builder
	 * @return a populated name builder
	 */
	private static X500NameBuilder createNameBuilder(final X509DistinguishedName distinguishedName) {
		final X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
		if (!isEmpty(distinguishedName.getCountry())) {
			nameBuilder.addRDN(BCStyle.C, distinguishedName.getCountry());
		}
		if (!isEmpty(distinguishedName.getLocality())) {
			nameBuilder.addRDN(BCStyle.L, distinguishedName.getLocality());
		}
		if (!isEmpty(distinguishedName.getOrganization())) {
			nameBuilder.addRDN(BCStyle.O, distinguishedName.getOrganization());
		}
		if (!isEmpty(distinguishedName.getOrganizationalUnit())) {
			nameBuilder.addRDN(BCStyle.OU, distinguishedName.getOrganizationalUnit());
		}
		if (!isEmpty(distinguishedName.getCommonName())) {
			nameBuilder.addRDN(BCStyle.CN, distinguishedName.getCommonName());
		}

		return nameBuilder;
	}

	/**
	 * Finds out whether a string can be considered empty.
	 *
	 * @param string the string to test
	 * @return whether the string is considered to be empty
	 */
	private static boolean isEmpty(final String string) {
		if (null == string) {
			return true;
		}

		return "".equals(string.trim());
	}

	@Override
	public X509Certificate build(final PrivateKey issuerPrivateKey) {

		// Create certificate signer instance with issuer private key.
		final ContentSigner signer;

		final JcaContentSignerBuilder jcaContentSignerBuilder = new JcaContentSignerBuilder(signatureAlgorithm);

		try {

			if (Provider.DEFAULT != provider) {
				jcaContentSignerBuilder.setProvider(provider.getProviderName());
			}

			signer = jcaContentSignerBuilder.build(issuerPrivateKey);

		} catch (final OperatorCreationException e) {
			throw new CryptoLibException(CREATE_CERTIFICATE_SIGNER_ERROR_MESSAGE, e);
		}

		// Generate certificate.
		try {
			return new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certBuilder.build(signer));
		} catch (final CertificateException e) {
			throw new CryptoLibException(BUILD_CERTIFICATE_ERROR_MESSAGE, e);
		}
	}

	@Override
	public void addExtension(final AbstractCertificateExtension extension) {
		final ExtensionBuilder extensionBuilder = new ExtensionBuilder(certBuilder);

		extensionBuilder.addExtension(extension);
	}

	/**
	 * Generates the X509 name builders.
	 */
	private void generateSubjectX500Builder() {
		subjectX509NameBuilder = createNameBuilder(subjectDn);
	}
}
