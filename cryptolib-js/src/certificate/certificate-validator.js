/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const X509Certificate = require('./x509-certificate');
const validator = require('../input-validator');

module.exports = CertificateValidator;

/**
 * @class CertificateValidator
 * @classdesc The certificate validator API. To instantiate this object, use the
 *            method {@link CertificateService.newValidator}.
 * @hideconstructor
 */
function CertificateValidator() {
	// Empty constructor
}

CertificateValidator.prototype = {
    /**
     * Validates the content of a certificate and returns an array of strings
     * which indicate which types of certificate validation, if any, failed.
     *
     * @function validate
     * @memberof CertificateValidator
     * @param {string}
     *            certificatePem The certificate to validate, in PEM format.
     * @param {Object}
     *            validationData The data against which to validate the
     *            certificate content.
     * @param {string}
     *            [validationData.subject] The subject validation data.
     * @param {string}
     *            [validationData.subject.commonName] The reference subject
     *            common name.
     * @param {string}
     *            [validationData.subject.organizationalUnit] The reference
     *            subject organizational unit.
     * @param {string}
     *            [validationData.subject.organization] The reference subject
     *            organization.
     * @param {string}
     *            [validationData.subject.country] The reference subject
     *            country.
     * @param {string}
     *            [validationData.issuer] The issuer validation data.
     * @param {string}
     *            [validationData.issuer.commonName] The reference issuer common
     *            name.
     * @param {string}
     *            [validationData.issuer.organizationalUnit] The reference
     *            issuer organizational unit.
     * @param {string}
     *            [validationData.issuer.organization] The reference issuer
     *            organization.
     * @param {string}
     *            [validationData.issuer.country] The reference issuer country.
     * @param {string}
     *            [validationData.time] The reference time, in ISO format (e.g.
     *            <code>'2014-10-23\'T\'15:20:11Z'</code>).
     * @param {string}
     *            [validationData.keyType] The reference key type
     * (<code>'CA'</code>, <code>'Sign'</code> or <code>'Encryption'</code>).
     * @param {string}
     *            [validationData.issuerCertificatePem] The issuer certificate,
     *            in PEM format, needed to verify this certificate's signature.
     * @returns {string[]} An array of strings indicating which types of
     *          certificate validation, if any, failed.
     * @throws {Error}
     *             If the input data validation fails.
     */
    validate: function (certificatePem, validationData) {
        validator.checkIsNonEmptyString(
            certificatePem, 'Certificate to validate, in PEM format');
        validator.checkIsObjectWithProperties(
            validationData, 'Certificate validation data');

		const certificate = new X509Certificate(certificatePem);
		const failedValidations = [];

		if (validationData.subject) {
            validateSubject(validationData.subject, certificate, failedValidations);
        }

        if (validationData.issuer) {
            validateIssuer(validationData.issuer, certificate, failedValidations);
        }

        if (validationData.time) {
            validateTime(validationData.time, certificate, failedValidations);
        }

        if (validationData.keyType) {
            validateKeyType(validationData.keyType, certificate, failedValidations);
        }

        if (validationData.issuerCertificatePem) {
            validateSignature(
                validationData.issuerCertificatePem, certificatePem,
                failedValidations);
        }

        return failedValidations;
    },

    /**
     * Validates a certificate chain provided as input. The validation process
     * loops through all certificates in the chain, starting with the leaf
     * certificate, until it reaches the root certificate. For each certificate,
     * except the root certificate, it checks that the following conditions
     * hold:
     * <ul>
     * <li>Subject DN (distinguished name) is that expected for given
     * certificate.</li>
     * <li>Issuer DN is same as subject DN of next certificate in chain.</li>
     * <li>Key type is that expected: <code>'Sign'</code> or
     * <code>'Encryption'</code> for leaf certificate and <code>'CA'</code>
     * for rest of certificates in chain.</li>
     * <li>Signature can be verified with public key of next certificate in
     * chain.</li>
     * <li>Starting time is earlier than ending time for given certificate.</li>
     * <li>Starting time is equal to or later than starting time of next
     * certificate in chain.</li>
     * <li>Ending time is equal to or earlier than ending time of next
     * certificate in chain.</li>
     * </ul>
     * In addition, if a non-null value is provided for the time reference, it
     * will be checked whether this time reference is within the dates of
     * validity of the leaf certificate.
     *
     * After the validation process has completed, a two dimensional array of
     * strings will be returned. If this array is empty, then the validation was
     * successful. Otherwise, each element in the first dimension of the array
     * will correspond to a single certificate that failed the validation, in
     * ascending order of certificate authority. Each element in the second
     * dimension will consist of an array of failed validation types for a given
     * certificate.
     *
     * @function validateChain
     * @memberof CertificateValidator
     * @param {Object}
     *            chain The chain of certificates to validate.
     * @param {Object}
     *            chain.leaf The data for the leaf certificate of the chain.
     * @param {string}
     *            chain.leaf.pem The leaf certificate, in PEM format.
     * @param {string}
     *            chain.leaf.keyType The key type of the leaf certificate
     * (<code>'Sign'</code> or <code>'Encryption'</code>).
     * @param {Object}
     *            chain.leaf.subject The subject DN of the leaf certificate.
     * @param {string}
     *            [chain.leaf.time] The time reference of the leaf certificate,
     *            in ISO format (e.g. <code>'2014-10-23\'T\'15:20:11Z'</code>).
     * @param {Object}
     *            chain.certificates The data for the intermediate certificates
     *            of the chain.
     * @param {string[]}
     *            chain.certificates.pems The array of intermediate
     *            certificates, each in PEM format. The array is in descending
     *            order of certificate authority.
     * @param {Object[]}
     *            chain.certificates.subjects The array of subject DN's for the
     *            intermediate certificates. The array is in descending order of
     *            authority.
     * @param {string}
     *            chain.root The root certificate, in PEM format.
     * @returns {string[][]} The two dimensional string array containing
     *          information about any failed validations.
     * @throws {Error}
     *             If the input data validation fails.
     */
    validateChain: function (chain) {
		const failedValidation = [];

		const rootCertificate = new X509Certificate(chain.root.pem);
		let issuer = {
			commonName: rootCertificate.subjectCommonName,
			organizationalUnit: rootCertificate.subjectOrganizationalUnit,
			organization: rootCertificate.subjectOrganization,
			country: rootCertificate.subjectCountry
		};
		let issuerCertificatePem = chain.root.pem;
		let previousNotBefore = rootCertificate.notBefore;
		let previousNotAfter = rootCertificate.notAfter;

		chain.intermediates.pems.reverse();
        chain.intermediates.subjects.reverse();
        for (let i = 0; i < chain.intermediates.pems.length; i++) {
			const validationData = {
				subject: chain.intermediates.subjects[i],
				issuer: issuer,
				keyType: 'CA',
				issuerCertificatePem: issuerCertificatePem
			};

			failedValidation[i] =
                this.validate(chain.intermediates.pems[i], validationData);

			const certificate = new X509Certificate(chain.intermediates.pems[i]);

			validateDateRange(certificate, failedValidation[i]);
            validateNotBefore(
                certificate.notBefore, previousNotBefore, failedValidation[i]);
            validateNotAfter(
                certificate.notAfter, previousNotAfter, failedValidation[i]);

            issuer = {
                commonName: certificate.subjectCommonName,
                organizationalUnit: certificate.subjectOrganizationalUnit,
                organization: certificate.subjectOrganization,
                country: certificate.subjectCountry
            };
            issuerCertificatePem = chain.intermediates.pems[i];
            previousNotBefore = certificate.notBefore;
            previousNotAfter = certificate.notAfter;
        }

        failedValidation.push(validateLeafCertificate(
            this, chain, issuer, issuerCertificatePem, previousNotBefore,
            previousNotAfter));

        failedValidation.reverse();

        return failedValidation;
    },

    /**
     * Flattens a two-dimensional array of certificate chain failed validations
     * into a one-dimensional array containing the same information. Each entry
     * in the flattened array will have the form:
     * <code>validation-type_certificate-index</code>, where
     * <code>validation-type</code> is the type of validation that failed and
     * <code>certificate-index</code> is the index in the certificate chain,
     * in ascending order of authority, of the given certificate that failed the
     * validation.
     *
     * @function flattenFailedValidations
     * @memberof CertificateValidator
     * @param {string[][]}
     *            failedValidations The two-dimensional array containing the
     *            failed validations.
     * @returns {string[]} The one-dimensional array containing the failed
     *          validations.
     * @throws {Error}
     *             If the input data validation fails.
     */
    flattenFailedValidations: function (failedValidations) {
        validator.checkIsTwoDimensionalArray(
            failedValidations, 'Certificate chain failed validation arrays');

		const flattenedFailedValidations = [];
		for (let i = 0; i < failedValidations.length; i++) {
            if (failedValidations[i] !== undefined) {
                for (let j = 0; j < failedValidations[i].length; j++) {
                    flattenedFailedValidations.push(
                        failedValidations[i][j].toLowerCase() + '_' + (i));
                }
            }
        }

        return flattenedFailedValidations;
    }
};

function validateSubject(subject, certificate, failedValidations) {
    if (subject.commonName !== certificate.subjectCommonName ||
        subject.organizationalUnit !== certificate.subjectOrganizationalUnit ||
        subject.organization !== certificate.subjectOrganization ||
        subject.country !== certificate.subjectCountry) {
        failedValidations.push('SUBJECT');
    }
}

function validateIssuer(issuer, certificate, failedValidations) {
    if (issuer.commonName !== certificate.issuerCommonName ||
        issuer.organizationalUnit !== certificate.issuerOrganizationalUnit ||
        issuer.organization !== certificate.issuerOrganization ||
        issuer.country !== certificate.issuerCountry) {
        failedValidations.push('ISSUER');
    }
}

function validateTime(isoDate, certificate, failedValidations) {
	const time = parseIsoDate(isoDate);

	if (time.toString() === 'Invalid Date' || time - certificate.notBefore < 0 ||
        time - certificate.notAfter > 0) {
        failedValidations.push('TIME');
    }
}

function validateKeyType(keyType, certificate, failedValidations) {
    if (!areBasicConstraintsValid(keyType, certificate) ||
        !isKeyUsageValid(keyType, certificate)) {
        failedValidations.push('KEY_TYPE');
    }
}

function areBasicConstraintsValid(keyType, certificate) {
	const basicConstraints = certificate.basicConstraints;

	return keyType !== 'CA' || (basicConstraints && basicConstraints.ca);
}

function isKeyUsageValid(keyType, certificate) {
	let returnValue = true;

	const keyUsageExtension = certificate.keyUsageExtension;
	if (!keyUsageExtension) {
        return false;
    }

    returnValue = checkKeyType(keyType, keyUsageExtension);

    return returnValue;
}

function checkKeyType(keyType, keyUsageExtension) {
    switch (keyType) {
        case 'CA':
            if (!isValidCA(keyUsageExtension)) {
                return false;
            }
            break;
        case 'Sign':
            if (!isValidSign(keyUsageExtension)) {
                return false;
            }
            break;
        case 'Encryption':
            if (!isValidEncryption(keyUsageExtension)) {
                return false;
            }
            break;
        default:
            return false;
    }

    return true;
}

function isValidCA(keyUsageExtension) {
    return keyUsageExtension.keyCertSign && keyUsageExtension.crlSign;
}

function isValidSign(keyUsageExtension) {
    return keyUsageExtension.digitalSignature && keyUsageExtension.nonRepudiation;
}

function isValidEncryption(keyUsageExtension) {
    return keyUsageExtension.keyEncipherment && keyUsageExtension.dataEncipherment;
}

function validateSignature(
    issuerCertificatePem, certificatePem, failedValidations) {
	const issuerCertificate = new X509Certificate(issuerCertificatePem);

	try {
        if (!issuerCertificate.verify(certificatePem)) {
            failedValidations.push('SIGNATURE');
        }
    } catch (error) {
        failedValidations.push('SIGNATURE');
    }
}

function parseIsoDate(isoDate) {
	const dateChunks = isoDate.split(/\D/);
	return new Date(Date.UTC(
        +dateChunks[0], --dateChunks[1], +dateChunks[2], +dateChunks[5],
        +dateChunks[6], +dateChunks[7], 0));
}

function validateDateRange(certificate, failedValidation) {
    if (certificate.notBefore > certificate.notAfter) {
        failedValidation.push('VALIDITY_PERIOD');
    }
}

function validateNotBefore(notBefore, previousNotBefore, failedValidation) {
    if (notBefore < previousNotBefore) {
        failedValidation.push('NOT_BEFORE');
    }
}

function validateNotAfter(notAfter, previousNotAfter, failedValidation) {
    if (notAfter > previousNotAfter) {
        failedValidation.push('NOT_AFTER');
    }
}

function validateLeafCertificate(
    certificateValidator, chain, issuer, issuerCertificatePem,
    previousNotBefore, previousNotAfter) {
	const validationData = {
		subject: chain.leaf.subject,
		issuer: issuer,
		keyType: chain.leaf.keyType,
		issuerCertificatePem: issuerCertificatePem
	};

	if (chain.leaf.time) {
        validationData.time = chain.leaf.time;
    }

	const failedValidations =
		certificateValidator.validate(chain.leaf.pem, validationData);

	const certificate = new X509Certificate(chain.leaf.pem);
	validateDateRange(certificate, failedValidations);
    validateNotBefore(
        certificate.notBefore, previousNotBefore, failedValidations);
    validateNotAfter(certificate.notAfter, previousNotAfter, failedValidations);

    return failedValidations;
}
