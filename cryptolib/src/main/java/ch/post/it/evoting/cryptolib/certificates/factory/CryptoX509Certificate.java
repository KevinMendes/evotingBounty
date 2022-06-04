/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsage;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.CertificateKeyUsageExtension;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.ExtensionType;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateConstants;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.LdapHelper;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.results.Result;
import ch.post.it.evoting.cryptolib.commons.results.ResultBuilder;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class which implements {@link CryptoAPIX509Certificate}.
 *
 * <p>Instances of this class are immutable.
 */
public final class CryptoX509Certificate implements CryptoAPIX509Certificate {

	private static final Logger LOGGER = LoggerFactory.getLogger(CryptoX509Certificate.class);

	private static final String PARSE_ISSUER_DN_ERROR_MESSAGE = "Could not parse issuer distinguished name.";
	private static final String PARSE_SUBJECT_DN_ERROR_MESSAGE = "Could not parse subject distinguished name.";
	private static final String GET_ENCODED_ERROR_MESSAGE = "Could not retrieve certificate as array of bytes.";

	private final X509Certificate certificate;
	private final boolean[] keyUsageFlags;
	private final X509DistinguishedName subjectDn;
	private final X509DistinguishedName issuerDn;

	/**
	 * Creates an object with X509 certificate that is going to be stored.
	 *
	 * @param certificate The X509 certificate that is going to be stored.
	 * @throws GeneralCryptoLibException if the subject distinguished name could not be parsed.
	 */
	public CryptoX509Certificate(final X509Certificate certificate) throws GeneralCryptoLibException {

		Validate.notNull(certificate, "X509 certificate");
		try {
			Validate.notNullOrEmpty(certificate.getEncoded(), "X509 certificate content");
		} catch (final CertificateEncodingException e) {
			throw new GeneralCryptoLibException("Could not validate content of X509 certificate.", e);
		}

		this.certificate = certificate;

		final LdapHelper ldapHelper = new LdapHelper();

		final String tmpSubjectDn = this.certificate.getSubjectX500Principal().getName();
		try {
			final String subjectCn = ldapHelper.getAttributeFromDistinguishedName(tmpSubjectDn, X509CertificateConstants.COMMON_NAME_ATTRIBUTE_NAME);
			final String subjectOrgUnit = ldapHelper
					.getAttributeFromDistinguishedName(tmpSubjectDn, X509CertificateConstants.ORGANIZATIONAL_UNIT_ATTRIBUTE_NAME);
			final String subjectOrg = ldapHelper.getAttributeFromDistinguishedName(tmpSubjectDn, X509CertificateConstants.ORGANIZATION_ATTRIBUTE_NAME);
			final String subjectLocality = ldapHelper.getAttributeFromDistinguishedName(tmpSubjectDn, X509CertificateConstants.LOCALITY_ATTRIBUTE_NAME);
			final String subjectCountry = ldapHelper.getAttributeFromDistinguishedName(tmpSubjectDn, X509CertificateConstants.COUNTRY_ATTRIBUTE_NAME);
			this.subjectDn = new X509DistinguishedName.Builder(subjectCn, subjectCountry).addOrganizationalUnit(subjectOrgUnit)
					.addOrganization(subjectOrg).addLocality(subjectLocality).build();

		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException(PARSE_SUBJECT_DN_ERROR_MESSAGE, e);
		}

		final String tmpIssuerDn = this.certificate.getIssuerX500Principal().getName();
		try {
			final String issuerCn = ldapHelper.getAttributeFromDistinguishedName(tmpIssuerDn, X509CertificateConstants.COMMON_NAME_ATTRIBUTE_NAME);
			final String issuerOrgUnit = ldapHelper
					.getAttributeFromDistinguishedName(tmpIssuerDn, X509CertificateConstants.ORGANIZATIONAL_UNIT_ATTRIBUTE_NAME);
			final String issuerOrg = ldapHelper.getAttributeFromDistinguishedName(tmpIssuerDn, X509CertificateConstants.ORGANIZATION_ATTRIBUTE_NAME);
			final String issuerLocality = ldapHelper.getAttributeFromDistinguishedName(tmpIssuerDn, X509CertificateConstants.LOCALITY_ATTRIBUTE_NAME);
			final String issuerCountry = ldapHelper.getAttributeFromDistinguishedName(tmpIssuerDn, X509CertificateConstants.COUNTRY_ATTRIBUTE_NAME);
			this.issuerDn = new X509DistinguishedName.Builder(issuerCn, issuerCountry).addOrganizationalUnit(issuerOrgUnit).addOrganization(issuerOrg)
					.addLocality(issuerLocality).build();
		} catch (final GeneralCryptoLibException e) {
			throw new GeneralCryptoLibException(PARSE_ISSUER_DN_ERROR_MESSAGE, e);
		}

		keyUsageFlags = this.certificate.getKeyUsage();
	}

	private static EnumSet<CertificateKeyUsage> getKeyUsageSetForCertificateType(final X509CertificateType certificateType) {

		final AbstractCertificateExtension[] certificateExtensions = certificateType.getExtensions();

		EnumSet<CertificateKeyUsage> certificateKeyUsages = EnumSet.noneOf(CertificateKeyUsage.class);
		for (final AbstractCertificateExtension extension : certificateExtensions) {
			if (extension.getExtensionType() == ExtensionType.KEY_USAGE) {
				certificateKeyUsages = ((CertificateKeyUsageExtension) extension).getKeyUsages();
			}
		}

		return certificateKeyUsages;
	}

	/**
	 * Returns the JCE X509 certificate.
	 *
	 * @return the JCE X509 certificate.
	 */
	@Override
	public X509Certificate getCertificate() {

		return certificate;
	}

	@Override
	public BigInteger getSerialNumber() {

		return certificate.getSerialNumber();
	}

	@Override
	public Date getNotBefore() {

		return certificate.getNotBefore();
	}

	@Override
	public Date getNotAfter() {

		return certificate.getNotAfter();
	}

	@Override
	public Result checkValidity() {

		Result validationResult;
		// If checkValidity method does not throw exception, then certificate is
		// valid.
		try {
			certificate.checkValidity();
			validationResult = new ResultBuilder().build();
		} catch (final CertificateExpiredException | CertificateNotYetValidException e) {
			LOGGER.error("Certificate is expired or not yet valid", e);
			validationResult = new ResultBuilder().add(e.getMessage()).build();
		}
		return validationResult;
	}

	@Override
	public boolean checkValidity(final Date date) throws GeneralCryptoLibException {

		Validate.notNull(date, "Date");

		// If checkValidity method does not throw exception, then date is within
		// the certificate's period of validity.
		try {
			certificate.checkValidity(date);
			return true;
		} catch (final CertificateExpiredException e) {
			LOGGER.error("Certificate is expired", e);
			return false;
		} catch (final CertificateNotYetValidException e) {
			LOGGER.error("Certificate is not yet valid", e);
			return false;
		}
	}

	@Override
	public X509DistinguishedName getSubjectDn() {

		return subjectDn;
	}

	@Override
	public X509DistinguishedName getIssuerDn() {

		return issuerDn;
	}

	@Override
	public PublicKey getPublicKey() {

		return certificate.getPublicKey();
	}

	@Override
	public boolean verify(final PublicKey issuerPublicKey) throws GeneralCryptoLibException {

		Validate.notNull(issuerPublicKey, "Issuer public key");
		Validate.notNullOrEmpty(issuerPublicKey.getEncoded(), "Issuer public key content");

		try {
			certificate.verify(issuerPublicKey);
		} catch (final InvalidKeyException | CertificateException | NoSuchProviderException | NoSuchAlgorithmException | SignatureException e) {
			LOGGER.error("Public key validation failed", e);
			return false;
		}
		return true;
	}

	@Override
	public boolean isCertificateType(final X509CertificateType certificateType) {

		if ((certificateType == X509CertificateType.CERTIFICATE_AUTHORITY
				&& certificate.getBasicConstraints() != X509CertificateConstants.IS_CERTIFICATE_AUTHORITY_BASIC_CONSTRAINTS_INTEGER_VALUE) || (
				certificateType != X509CertificateType.CERTIFICATE_AUTHORITY
						&& certificate.getBasicConstraints() == X509CertificateConstants.IS_CERTIFICATE_AUTHORITY_BASIC_CONSTRAINTS_INTEGER_VALUE)) {
			return false;
		}

		return hasKeyUsageSet(getKeyUsageSetForCertificateType(certificateType));
	}

	@Override
	public byte[] getEncoded() {

		try {
			return certificate.getEncoded();
		} catch (final CertificateEncodingException e) {
			throw new CryptoLibException(GET_ENCODED_ERROR_MESSAGE, e);
		}
	}

	@Override
	public byte[] getPemEncoded() {

		try {
			return PemUtils.certificateToPem(certificate).getBytes(StandardCharsets.UTF_8);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException("Could not convert certificate to PEM format.", e);
		}
	}

	private boolean hasKeyUsageSet(final EnumSet<CertificateKeyUsage> certificateKeyUsages) {

		if (keyUsageFlags == null) {
			return false;
		}

		int keyUsageCounter = 0;

		for (final CertificateKeyUsage keyUsage : CertificateKeyUsage.values()) {
			if (certificateKeyUsages.contains(keyUsage) && keyUsageFlags[keyUsage.ordinal()]) {
				keyUsageCounter++;
			}
		}

		return keyUsageCounter == certificateKeyUsages.size();
	}
}
