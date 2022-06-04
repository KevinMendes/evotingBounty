/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;

/**
 * Class that validates chains of certificates.
 */
public class X509CertificateChainValidator {

	private static final String FAILED_VALIDATIONS_FORMAT = "%s_%d";

	private final X509Certificate[] certificateChain;

	private final X509Certificate leafCertificate;

	private final X509CertificateType leafCertificateKeyType;

	private final X509DistinguishedName leafCertificateSubjectDn;

	private final X509DistinguishedName[] subjectDns;

	private final Date timeReference;

	private final X509Certificate trustedCertificate;

	private final int certificateChainLength;

	private final int subjectDnsLength;

	/**
	 * Creates a certificate chain validator for the provided input arguments.
	 *
	 * @param leafCertificate          the leaf certificate.
	 * @param leafCertificateKeyType   the leaf certificate type.
	 * @param leafCertificateSubjectDn the leaf certificate subject distinguished name.
	 * @param timeReference            a time reference that must be within the dates of validity of the leaf certificate.
	 * @param certificateChain         the certificate chain of the leaf certificate.
	 * @param subjectDns               the subject distinguished names of the certificate chain.
	 * @param trustedCertificate       the trusted certificate.
	 * @throws GeneralCryptoLibException if the arguments are invalid.
	 */
	public X509CertificateChainValidator(final X509Certificate leafCertificate, final X509CertificateType leafCertificateKeyType,
			final X509DistinguishedName leafCertificateSubjectDn, final Date timeReference, final X509Certificate[] certificateChain,
			final X509DistinguishedName[] subjectDns, final X509Certificate trustedCertificate) throws GeneralCryptoLibException {

		super();

		Validate.notNull(leafCertificate, "Leaf certificate");
		Validate.notNull(leafCertificateKeyType, "Leaf certificate key type");
		Validate.notNull(leafCertificateSubjectDn, "Leaf certificate subject distinguished name");
		Validate.notNull(trustedCertificate, "Trusted certificate");

		if (certificateChain == null || certificateChain.length == 0) {
			this.certificateChain = new X509Certificate[0];
		} else {
			this.certificateChain = Arrays.copyOf(certificateChain, certificateChain.length);
		}

		if (subjectDns == null || subjectDns.length == 0) {
			this.subjectDns = new X509DistinguishedName[0];
		} else {
			this.subjectDns = Arrays.copyOf(subjectDns, subjectDns.length);
		}

		certificateChainLength = this.certificateChain.length;
		subjectDnsLength = this.subjectDns.length;
		if (certificateChainLength != subjectDnsLength) {
			throw new GeneralCryptoLibException("The number of certificates in the chain " + certificateChainLength
					+ " is different from the number of corresponding subject distinguished names " + subjectDnsLength);
		}

		this.leafCertificate = leafCertificate;
		this.leafCertificateKeyType = leafCertificateKeyType;
		this.leafCertificateSubjectDn = leafCertificateSubjectDn;
		this.timeReference = timeReference == null ? null : new Date(timeReference.getTime());
		this.trustedCertificate = trustedCertificate;
	}

	/**
	 * Creates a certificate chain validator for the provided input arguments.
	 *
	 * @param leafCertificate          the leaf certificate.
	 * @param leafCertificateKeyType   the leaf certificate type.
	 * @param leafCertificateSubjectDn the leaf certificate subject distinguished name.
	 * @param certificateChain         the certificate chain of the leaf certificate.
	 * @param subjectDns               the subject distinguished names of the certificate chain.
	 * @param trustedCertificate       the trusted certificate.
	 * @throws GeneralCryptoLibException if the arguments are invalid.
	 */
	public X509CertificateChainValidator(final X509Certificate leafCertificate, final X509CertificateType leafCertificateKeyType,
			final X509DistinguishedName leafCertificateSubjectDn, final X509Certificate[] certificateChain, final X509DistinguishedName[] subjectDns,
			final X509Certificate trustedCertificate) throws GeneralCryptoLibException {

		this(leafCertificate, leafCertificateKeyType, leafCertificateSubjectDn, null, certificateChain, subjectDns, trustedCertificate);
	}

	private static List<String> validateTime(final Date timeReference, final CryptoX509Certificate cryptoCertificate)
			throws GeneralCryptoLibException {

		final List<String> failedValidations = new ArrayList<>();
		final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addDate(timeReference).build();

		final X509CertificateValidator validator = new X509CertificateValidator(cryptoCertificate, validationData, X509CertificateValidationType.DATE);
		final X509CertificateValidationResult validationResult = validator.validate();
		if (!validationResult.isValidated()) {

			failedValidations.add("Time");
		}

		return failedValidations;
	}

	/**
	 * Validates the certificate information provided to the constructor. The validation process loops through all certificates, starting with the
	 * leaf certificate, until it reaches the trusted certificate. For each certificate, except the trusted certificate, it checks that the following
	 * conditions hold:
	 *
	 * <ul>
	 *   <li>Subject DN is that expected for given certificate.
	 *   <li>Issuer DN is same as subject DN of next certificate in chain.
	 *   <li>Key type is that expected: "signing" or "encryption" for leaf certificate and "CA" for
	 *       rest of certificates in chain.
	 *   <li>Signature can be verified with public key of next certificate in chain.
	 *   <li>Starting time is earlier than ending time.
	 *   <li>Starting time is equal to or later than starting time of next certificate in chain.
	 *   <li>Ending time is equal to or earlier than ending time of next certificate in chain.
	 * </ul>
	 * <p>
	 * In addition, if a non-null value is provided to the constructor for the time reference, it will
	 * be checked whether this time reference is within the dates of validity of the leaf certificate.
	 * After the validation process has completed, a list of strings will be returned. If this list is
	 * empty, then the validation was successful. Otherwise, the list will contain string identifiers
	 * for each type of validation that failed.
	 *
	 * @return a list of failed validations.
	 * @throws GeneralCryptoLibException if a distinguished name could not be parsed or an invalid validation type was requested for the validation.
	 */
	public List<String> validate() throws GeneralCryptoLibException {

		final List<String> failedValidations = new ArrayList<>();

		final List<X509Certificate> certList = new ArrayList<>();

		certList.add(leafCertificate);
		certList.addAll(Arrays.asList(certificateChain));

		certList.add(trustedCertificate);
		final X509Certificate[] allChain = certList.toArray(new X509Certificate[certificateChainLength + 2]);

		final List<X509DistinguishedName> nameList = new ArrayList<>();
		nameList.add(leafCertificateSubjectDn);
		nameList.addAll(Arrays.asList(subjectDns));
		nameList.add(null);
		final X509DistinguishedName[] allSubjects = nameList.toArray(new X509DistinguishedName[subjectDnsLength + 2]);

		for (int pos = 0; pos < allChain.length - 1; pos++) {

			final int nextPos = pos + 1;
			final X509Certificate elementX = allChain[pos];
			final X509Certificate elementXplus1 = allChain[nextPos];

			final X509CertificateType expectedKeyType;
			if (pos == 0) {
				expectedKeyType = leafCertificateKeyType;
			} else {
				expectedKeyType = X509CertificateType.CERTIFICATE_AUTHORITY;
			}

			final X509DistinguishedName issuerSubjectDn = new CryptoX509Certificate(allChain[nextPos]).getSubjectDn();

			final X509CertificateValidationData validationData = new X509CertificateValidationData.Builder().addSubjectDn(allSubjects[pos])
					.addIssuerDn(issuerSubjectDn).addKeyType(expectedKeyType).addCaPublicKey(elementXplus1.getPublicKey()).build();

			final X509CertificateValidator validator = new X509CertificateValidator(new CryptoX509Certificate(elementX), validationData,
					X509CertificateValidationType.SUBJECT, X509CertificateValidationType.ISSUER, X509CertificateValidationType.KEY_TYPE,
					X509CertificateValidationType.SIGNATURE);

			final X509CertificateValidationResult validationResult = validator.validate();
			if (!validationResult.isValidated()) {
				final List<X509CertificateValidationType> failedValidationTypes = validationResult.getFailedValidationTypes();
				for (final X509CertificateValidationType x509CertificateValidationType : failedValidationTypes) {
					failedValidations.add(String.format(FAILED_VALIDATIONS_FORMAT, x509CertificateValidationType, pos));
				}
			}

			if (elementX.getNotBefore().after(elementX.getNotAfter())) {
				failedValidations.add(String.format(FAILED_VALIDATIONS_FORMAT, "ValidityPeriod", pos));
			}
			if (elementX.getNotBefore().before(elementXplus1.getNotBefore())) {
				failedValidations.add(String.format(FAILED_VALIDATIONS_FORMAT, "notBefore", pos));
			}
			if (elementX.getNotAfter().after(elementXplus1.getNotAfter())) {
				failedValidations.add(String.format(FAILED_VALIDATIONS_FORMAT, "notAfter", pos));
			}
		}

		if (timeReference != null) {
			final CryptoX509Certificate cryptoLeafX509Certificate = new CryptoX509Certificate(leafCertificate);
			failedValidations.addAll(validateTime(timeReference, cryptoLeafX509Certificate));
		}

		return failedValidations;
	}
}
