/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.validation;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationData;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationType;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateChainValidator;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateValidator;

public class CertificateValidationServiceImpl implements CertificateValidationService {

	@Override
	public CertificateValidationResult validateCertificate(final Certificate certificateToValidate, final Certificate parentCertificate)
			throws GeneralCryptoLibException, CryptographicOperationException {

		boolean isCACertificate = false;
		X509CertificateType type = CertificateTools.extractTypeFromCertificate((X509Certificate) certificateToValidate);
		List<String> validationErrors = new ArrayList<>();
		if (type == X509CertificateType.CERTIFICATE_AUTHORITY) {
			isCACertificate = true;
			validationErrors.add(CertificateValidationErrorType.IS_CERTIFICATE_AUTHORITY.getDescription());
		}

		X509Certificate certLeaf = (X509Certificate) certificateToValidate;
		X509Certificate certTrusted = (X509Certificate) parentCertificate;
		X509DistinguishedName subjectDnsLeaf = getDistinguishName(certLeaf);
		X509Certificate[] certChain = {};
		X509DistinguishedName[] subjectDns = {};

		X509CertificateChainValidator x509CertificateChainValidator = new X509CertificateChainValidator(certLeaf, type, subjectDnsLeaf, certChain,
				subjectDns, certTrusted);
		final List<String> validationChain = x509CertificateChainValidator.validate();
		boolean chainValidationResult = validationChain.isEmpty();
		if (!validationChain.isEmpty()) {
			String error = CertificateValidationErrorType.CHAIN_VALIDATION_FAILED.getDescription().concat(":")
					.concat(StringUtils.join(validationChain.stream(), ","));
			validationErrors.add(error);
		}

		CryptoX509Certificate cryptoX509CertificateToValidate = CertificateTools.getCryptoX509Certificate(certificateToValidate);
		CryptoX509Certificate cryptoX509ParentCertificate = CertificateTools.getCryptoX509Certificate(parentCertificate);
		X509CertificateValidationData validationData = createValidationDataForNonCACertificate(cryptoX509CertificateToValidate,
				cryptoX509ParentCertificate);
		List<X509CertificateValidationType> x509CertificateValidationType = createValidationsListWithoutType();

		X509CertificateValidator x509CertificateValidator = createValidator(cryptoX509CertificateToValidate, validationData,
				x509CertificateValidationType);

		final X509CertificateValidationResult x509CertificateValidationResult = performValidations(x509CertificateValidator);
		if (!x509CertificateValidationResult.isValidated()) {
			String error = CertificateValidationErrorType.CERTIFICATE_VALIDATION_FAILED.getDescription().concat(":")
					.concat(StringUtils.join(x509CertificateValidationResult.getFailedValidationTypes(), ','));
			validationErrors.add(error);
		}

		CertificateValidationResult result = new CertificateValidationResult();
		result.setValid(!isCACertificate && chainValidationResult && x509CertificateValidationResult.isValidated());
		result.setValidationErrorMessages(validationErrors);
		return result;
	}

	@Override
	public X509CertificateValidationResult validateRootCertificate(final Certificate certificateToValidate) throws CryptographicOperationException {

		CryptoX509Certificate cryptoX509CertificateToValidate = CertificateTools.getCryptoX509Certificate(certificateToValidate);

		X509CertificateValidationData validationData;
		try {
			validationData = createValidationDataForRootCertificate(cryptoX509CertificateToValidate);
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("Exception while creating validation data for Root certificate", e);
		}

		List<X509CertificateValidationType> x509CertificateValidationType = createValidationsList();

		X509CertificateValidator x509CertificateValidator = createValidator(cryptoX509CertificateToValidate, validationData,
				x509CertificateValidationType);

		return performValidations(x509CertificateValidator);
	}

	@Override
	public X509CertificateValidationResult validateIntermediateCACertificate(final Certificate certificateToValidate,
			final Certificate parentCertificate) throws CryptographicOperationException, GeneralCryptoLibException {
		X509CertificateValidationResult validateRootCertificate = validateParentCertificate(parentCertificate);
		if (!validateRootCertificate.isValidated()) {
			return validateRootCertificate;
		}
		CryptoX509Certificate cryptoX509CertificateToValidate = CertificateTools.getCryptoX509Certificate(certificateToValidate);
		CryptoX509Certificate cryptoX509ParentCertificate = CertificateTools.getCryptoX509Certificate(parentCertificate);

		X509CertificateValidationData validationData = createValidationDataForIntermediateCACertificate(cryptoX509CertificateToValidate,
				cryptoX509ParentCertificate);

		List<X509CertificateValidationType> x509CertificateValidationType = createValidationsList();

		X509CertificateValidator x509CertificateValidator = createValidator(cryptoX509CertificateToValidate, validationData,
				x509CertificateValidationType);

		return performValidations(x509CertificateValidator);
	}

	private X509CertificateValidationResult validateParentCertificate(Certificate certificateToValidate)
			throws GeneralCryptoLibException, CryptographicOperationException {
		CryptoX509Certificate cryptoX509CertificateToValidate = CertificateTools.getCryptoX509Certificate(certificateToValidate);

		X509CertificateValidationData validationData = createValidationDataForRootCertificate(cryptoX509CertificateToValidate);

		List<X509CertificateValidationType> x509CertificateValidationType = createDateAndTypeValidationList();

		X509CertificateValidator x509CertificateValidator = createValidator(cryptoX509CertificateToValidate, validationData,
				x509CertificateValidationType);

		return performValidations(x509CertificateValidator);
	}

	private List<X509CertificateValidationType> createDateAndTypeValidationList() {
		List<X509CertificateValidationType> x509CertificateValidationType = new ArrayList<>();
		x509CertificateValidationType.add(X509CertificateValidationType.DATE);
		x509CertificateValidationType.add(X509CertificateValidationType.KEY_TYPE);
		return x509CertificateValidationType;
	}

	private List<X509CertificateValidationType> createValidationsList() {

		List<X509CertificateValidationType> x509CertificateValidationType = new ArrayList<>();
		x509CertificateValidationType.add(X509CertificateValidationType.SIGNATURE);
		x509CertificateValidationType.add(X509CertificateValidationType.DATE);
		x509CertificateValidationType.add(X509CertificateValidationType.KEY_TYPE);
		return x509CertificateValidationType;
	}

	private List<X509CertificateValidationType> createValidationsListWithoutType() {

		List<X509CertificateValidationType> x509CertificateValidationType = new ArrayList<>();
		x509CertificateValidationType.add(X509CertificateValidationType.SIGNATURE);
		x509CertificateValidationType.add(X509CertificateValidationType.DATE);
		return x509CertificateValidationType;
	}

	private X509CertificateValidationResult performValidations(final X509CertificateValidator x509CertificateValidator)
			throws CryptographicOperationException {

		X509CertificateValidationResult x509CertificateValidationResult;
		try {
			x509CertificateValidationResult = x509CertificateValidator.validate();
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured while performing validation", e);
		}
		return x509CertificateValidationResult;
	}

	private X509CertificateValidator createValidator(final CryptoX509Certificate cryptoX509CertificateToValidate,
			final X509CertificateValidationData validationData, final List<X509CertificateValidationType> x509CertificateValidationType)
			throws CryptographicOperationException {

		X509CertificateValidator x509CertificateValidator;
		try {
			x509CertificateValidator = new X509CertificateValidator(cryptoX509CertificateToValidate, validationData,
					x509CertificateValidationType.toArray(new X509CertificateValidationType[x509CertificateValidationType.size()]));
		} catch (GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured while trying to create a certificate validator", e);
		}
		return x509CertificateValidator;
	}

	private X509CertificateValidationData createValidationDataForRootCertificate(final CryptoX509Certificate cryptoX509CertificateToValidate)
			throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		return new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(cryptoX509CertificateToValidate.getSubjectDn())
				.addIssuerDn(cryptoX509CertificateToValidate.getIssuerDn()).addKeyType(X509CertificateType.CERTIFICATE_AUTHORITY)
				.addCaPublicKey(cryptoX509CertificateToValidate.getPublicKey()).build();
	}

	private X509CertificateValidationData createValidationDataForIntermediateCACertificate(
			final CryptoX509Certificate cryptoX509CertificateToValidate, final CryptoX509Certificate parentCertificate)
			throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		return new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(cryptoX509CertificateToValidate.getSubjectDn())
				.addIssuerDn(cryptoX509CertificateToValidate.getIssuerDn()).addKeyType(X509CertificateType.CERTIFICATE_AUTHORITY)
				.addCaPublicKey(parentCertificate.getPublicKey()).build();
	}

	private X509CertificateValidationData createValidationDataForNonCACertificate(final CryptoX509Certificate cryptoX509CertificateToValidate,
			final CryptoX509Certificate parentCertificate) throws GeneralCryptoLibException {

		Date date = new Date(System.currentTimeMillis());

		return new X509CertificateValidationData.Builder().addDate(date).addSubjectDn(cryptoX509CertificateToValidate.getSubjectDn())
				.addIssuerDn(parentCertificate.getIssuerDn()).addCaPublicKey(parentCertificate.getPublicKey()).build();
	}

	private X509DistinguishedName getDistinguishName(final X509Certificate x509Cert) throws GeneralCryptoLibException {
		CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate(x509Cert);
		return wrappedCertificate.getSubjectDn();
	}

	@Override
	public boolean validateCertificateChain(X509Certificate leafCert, X509DistinguishedName leafCertSubjectDn, X509CertificateType leafCertType,
			X509Certificate[] intermediateCertChain, X509DistinguishedName[] intermediateCertSubjectDns, X509Certificate rootCert)
			throws GeneralCryptoLibException {
		X509CertificateChainValidator certificateChainValidator = new X509CertificateChainValidator(leafCert, leafCertType, leafCertSubjectDn,
				intermediateCertChain, intermediateCertSubjectDns, rootCert);
		final List<String> errors = certificateChainValidator.validate();
		return errors.isEmpty();

	}

}
