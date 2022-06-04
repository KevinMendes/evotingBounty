/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import java.security.PublicKey;
import java.util.Date;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class that contains the data to validate against the content of a {@link java.security.cert.X509Certificate}.
 */
public final class X509CertificateValidationData {

	private final Date date;

	private final X509DistinguishedName subjectDn;

	private final X509DistinguishedName issuerDn;

	private final X509CertificateType certType;

	private final PublicKey caPublicKey;

	/**
	 * Creates an instance using parameters which are read from the {@code builder}.
	 *
	 * @param builder builder containing all components to be included in the {@code X509CertificateValidationData}.
	 */
	private X509CertificateValidationData(final Builder builder) {

		if (builder.date == null) {
			date = null;
		} else {
			date = new Date(builder.date.getTime());
		}
		subjectDn = builder.subjectDn;
		issuerDn = builder.issuerDn;
		certType = builder.certType;
		caPublicKey = builder.caPublicKey;
	}

	public Date getDate() {

		if (date != null) {
			return new Date(date.getTime());
		}
		return null;
	}

	/**
	 * Returns the subject {@link X509DistinguishedName} to check against the subject distinguished name of the {@link
	 * java.security.cert.X509Certificate}.
	 */
	public X509DistinguishedName getSubjectDn() {
		return subjectDn;
	}

	/**
	 * Returns the issuer {@link X509DistinguishedName} to check against the issuer distinguished name of the {@link
	 * java.security.cert.X509Certificate}.
	 */
	public X509DistinguishedName getIssuerDn() {
		return issuerDn;
	}

	/**
	 * Returns the {@link X509CertificateType} to check against the certificate type of the {@link java.security.cert.X509Certificate}.
	 */
	public X509CertificateType getCertificateType() {
		return certType;
	}

	/**
	 * Returns the CA {@link java.security.PublicKey} to check the signature of the {@link java.security.cert.X509Certificate}.
	 */
	public PublicKey getCaPublicKey() {

		return caPublicKey;
	}

	/**
	 * Builder class for creating a {@link X509CertificateValidationData}.
	 */
	public static class Builder {

		private Date date;

		private X509DistinguishedName subjectDn;

		private X509DistinguishedName issuerDn;

		private X509CertificateType certType;

		private PublicKey caPublicKey;

		/**
		 * Adds the {@link java.util.Date} to check against the validity dates of the {@link java.security.cert.X509Certificate}.
		 *
		 * @param date the {@link java.util.Date} to check.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the date is null.
		 */
		public Builder addDate(final Date date) throws GeneralCryptoLibException {

			Validate.notNull(date, "Date");

			this.date = new Date(date.getTime());

			return this;
		}

		/**
		 * Adds the subject {@link X509DistinguishedName} to check against the subject distinguished name of the {@link
		 * java.security.cert.X509Certificate}.
		 *
		 * @param subjectDn the subject {@link X509DistinguishedName} to check.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the subject distinguished name is null.
		 */
		public Builder addSubjectDn(final X509DistinguishedName subjectDn) throws GeneralCryptoLibException {

			Validate.notNull(subjectDn, "Subject distinguished name");

			this.subjectDn = subjectDn;

			return this;
		}

		/**
		 * Adds the issuer {@link X509DistinguishedName} to check against the issuer distinguished name of the {@link
		 * java.security.cert.X509Certificate}.
		 *
		 * @param issuerDn the issuer {@link X509DistinguishedName} to check.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the issuer distinguished name is null.
		 */
		public Builder addIssuerDn(final X509DistinguishedName issuerDn) throws GeneralCryptoLibException {

			Validate.notNull(issuerDn, "Issuer distinguished name");

			this.issuerDn = issuerDn;

			return this;
		}

		/**
		 * Adds the certificate type, encapsulated in an {@link X509CertificateType} object, to check against the certificate type of the {@link
		 * java.security.cert.X509Certificate}.
		 *
		 * @param certType the {@link X509CertificateType} to check.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the certificate type is null.
		 * @see X509CertificateType
		 */
		public Builder addKeyType(final X509CertificateType certType) throws GeneralCryptoLibException {

			Validate.notNull(certType, "Certificate type");

			this.certType = certType;

			return this;
		}

		/**
		 * Adds the certificate authority {@link java.security.PublicKey} to check the signature of the {@link java.security.cert.X509Certificate} .
		 *
		 * @param caPublicKey the CA {@link java.security.PublicKey}.
		 * @return updated builder.
		 * @throws GeneralCryptoLibException if the certificate authority public key is null.
		 */
		public Builder addCaPublicKey(final PublicKey caPublicKey) throws GeneralCryptoLibException {

			Validate.notNull(caPublicKey, "Issuer public key");
			Validate.notNullOrEmpty(caPublicKey.getEncoded(), "Issuer public key content");

			this.caPublicKey = caPublicKey;

			return this;
		}

		/**
		 * Builds the {@link X509CertificateValidationData}, based on the components that have been added to the builder.
		 *
		 * @return the {@link X509CertificateValidationData} with added components.
		 */
		public X509CertificateValidationData build() {

			return new X509CertificateValidationData(this);
		}
	}
}
