/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class to validate the content of a {@link CryptoX509Certificate}.
 */
public class X509CertificateValidator {

	private final CryptoX509Certificate cryptoX509Cert;

	private final X509CertificateValidationData validationData;

	private final Set<X509CertificateValidationType> validationTypes;

	private final List<X509CertificateValidationType> failedValidationTypes;

	/**
	 * Creates a certificate validator instance.
	 *
	 * @param cryptoX509Cert  the {@link CryptoX509Certificate} whose content is to be validated.
	 * @param validationData  the {@link X509CertificateValidationData} against which the content of the {@link CryptoX509Certificate} is to be
	 *                        validated.
	 * @param validationTypes the array of {@link X509CertificateValidationType} objects specifying the types of validations to apply to the {@link
	 *                        CryptoX509Certificate} .
	 * @throws GeneralCryptoLibException if the input validation fails.
	 */
	public X509CertificateValidator(final CryptoX509Certificate cryptoX509Cert, final X509CertificateValidationData validationData,
			final X509CertificateValidationType... validationTypes) throws GeneralCryptoLibException {

		Validate.notNull(cryptoX509Cert, "Certificate to validate");
		Validate.notNull(validationData, "Validation data");
		Validate.notNullOrEmpty(validationTypes, "Validation type array");
		for (final X509CertificateValidationType validationType : validationTypes) {
			Validate.notNull(validationType, "A validation type");
		}

		this.cryptoX509Cert = cryptoX509Cert;

		this.validationData = validationData;

		this.validationTypes = new HashSet<>();
		for (final X509CertificateValidationType validationType : validationTypes) {
			if (validationType == null) {
				throw new CryptoLibException("Null certificate validation type provided for certificate content validation.");
			}

			this.validationTypes.add(validationType);
		}

		failedValidationTypes = new ArrayList<>();
	}

	/**
	 * Validates the content of the {@link CryptoX509Certificate}.
	 *
	 * @return a {@link X509CertificateValidationResult} indicating any validations that failed.
	 * @throws GeneralCryptoLibException if the validation process fails.
	 */
	public X509CertificateValidationResult validate() throws GeneralCryptoLibException {

		for (final X509CertificateValidationType validationType : validationTypes) {
			switch (validationType) {
			case DATE:
				if (!validateDate()) {
					failedValidationTypes.add(X509CertificateValidationType.DATE);
				}
				break;
			case SUBJECT:
				if (!validateSubjectDn()) {
					failedValidationTypes.add(X509CertificateValidationType.SUBJECT);
				}
				break;
			case ISSUER:
				if (!validateIssuerDn()) {
					failedValidationTypes.add(X509CertificateValidationType.ISSUER);
				}
				break;
			case KEY_TYPE:
				if (!validateKeyType()) {
					failedValidationTypes.add(X509CertificateValidationType.KEY_TYPE);
				}
				break;
			default:
				if (!validateSignature()) {
					failedValidationTypes.add(X509CertificateValidationType.SIGNATURE);
				}
				break;
			}
		}

		final X509CertificateValidationType[] tmpFailedValidationTypes = new X509CertificateValidationType[this.failedValidationTypes.size()];
		this.failedValidationTypes.toArray(tmpFailedValidationTypes);

		return new X509CertificateValidationResult(this.failedValidationTypes.isEmpty(), tmpFailedValidationTypes);
	}

	private boolean validateDate() throws GeneralCryptoLibException {

		final Date date = validationData.getDate();

		Validate.notNull(date, "Validity date of certificate validation data");

		return cryptoX509Cert.checkValidity(date);
	}

	private boolean validateSubjectDn() throws GeneralCryptoLibException {

		final X509DistinguishedName subjectDn = validationData.getSubjectDn();

		Validate.notNull(subjectDn, "Subject DN of certificate validation data");

		return subjectDn.equals(cryptoX509Cert.getSubjectDn());
	}

	private boolean validateIssuerDn() throws GeneralCryptoLibException {

		final X509DistinguishedName issuerDn = validationData.getIssuerDn();

		Validate.notNull(issuerDn, "Issuer DN of certificate validation data");

		return issuerDn.equals(cryptoX509Cert.getIssuerDn());
	}

	private boolean validateKeyType() throws GeneralCryptoLibException {

		final X509CertificateType certType = validationData.getCertificateType();

		Validate.notNull(certType, "Certificate type of certificate validation data");

		return cryptoX509Cert.isCertificateType(certType);
	}

	private boolean validateSignature() throws GeneralCryptoLibException {

		final PublicKey caPublicKey = validationData.getCaPublicKey();

		Validate.notNull(caPublicKey, "CA public key of certificate validation data");

		return cryptoX509Cert.verify(caPublicKey);
	}
}
