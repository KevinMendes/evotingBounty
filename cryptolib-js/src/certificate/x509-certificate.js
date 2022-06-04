/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const BasicConstraints = require('./basic-constraints');
const KeyUsageExtension = require('./key-usage-extension');
const validator = require('../input-validator');
const constants = require('./constants');
const forge = require('node-forge');

module.exports = X509Certificate;

/**
 * @class X509Certificate
 * @classdesc Encapsulates an X.509 certificate. To instantiate this object, use
 *            the method {@link CertificateService.newX509Certificate}.
 * @property {string} publicKey The public key of the certificate, in PEM
 *           format.
 * @property {Date} notBefore The starting time of the certificate's validity.
 * @property {Date} notAfter The ending time of the certificate's validity.
 * @property {string} serialNumber The serial number of the certificate.
 * @property {string} subjectCommonName The subject common name of the
 *           certificate.
 * @property {string} subjectOrganizationalUnit The subject organizational unit
 *           of the certificate.
 * @property {string} subjectOrganization The subject organization of the
 *           certificate.
 * @property {string} subjectLocality The subject locality of the certificate.
 * @property {string} subjectCountry The subject country of the certificate.
 * @property {string} issuerCommonName The issuer common name of the
 *           certificate.
 * @property {string} issuerOrganizationalUnit The issuer organizational unit of
 *           the certificate.
 * @property {string} issuerOrganization The issuer organization of the
 *           certificate.
 * @property {string} issuerLocality The issuer locality of the certificate.
 * @property {string} issuerCountry The issuer country of the certificate.
 * @property {BasicConstraints} basicConstraints The basic constraints of the
 *           certificate.
 * @property {KeyUsageExtension} keyUsageExtension The key usage extension of
 *           the certificate.
 */
function X509Certificate(certificate) {
	const _forgeCertificate = forge.pki.certificateFromPem(certificate, true);

	validator.checkIsDefinedAndNotNull(
        _forgeCertificate.publicKey, 'Public key of certificate');
    this.publicKey = forge.pki.publicKeyToPem(_forgeCertificate.publicKey, 64);

    validator.checkIsDefinedAndNotNull(
        _forgeCertificate.validity.notBefore,
        'Starting time of validity of certificate');
    this.notBefore = _forgeCertificate.validity.notBefore;

    validator.checkIsDefinedAndNotNull(
        _forgeCertificate.validity.notAfter,
        'Ending time of validity of certificate');
    this.notAfter = _forgeCertificate.validity.notAfter;

    this.serialNumber = _forgeCertificate.serialNumber;
    if (!this.serialNumber) {
        this.serialNumber = '';
    }

    createSubject(this, _forgeCertificate);
    createIssuer(this, _forgeCertificate);

	const constraints = _forgeCertificate.getExtension({name: 'basicConstraints'});
	this.basicConstraints =
        (constraints) ? new BasicConstraints(constraints) : null;

	const extension = _forgeCertificate.getExtension({name: 'keyUsage'});
	this.keyUsageExtension =
        (extension) ? new KeyUsageExtension(extension) : null;

    /**
     * Verifies that the certificate provided as input was signed by this
     * certificate.
     *
     * @function verify
     * @memberof X509Certificate
     * @param {string}
     *            certificate The certificate whose signature is to be verified,
     *            in PEM format.
     * @returns {boolean} <code>true</code> if the certificate provided as
     *          input was signed by this certificate, <code>false</code>
     *          otherwise.
     * @throws {Error}
     *             If the input data validation fails.
     */
    this.verify = function (certificate) {
        validator.checkIsNonEmptyString(
            certificate,
            'Certificate, in PEM format, containing signature to be verified');

        return _forgeCertificate.verify(forge.pki.certificateFromPem(certificate));
    };

    /**
     * Serializes this certificate into its PEM string representation.
     *
     * @function toPem
     * @memberof X509Certificate
     * @returns {string} The PEM string representation of this certificate.
     */
    this.toPem = function () {
        return forge.pki.certificateToPem(
            _forgeCertificate, constants.PEM_LINE_LENGTH);
    };

    return Object.freeze(this);
}

function createSubject(that, forgeCertificate) {
	let field = forgeCertificate.subject.getField({name: 'commonName'});
	that.subjectCommonName = (field) ? field.value : '';
    field = forgeCertificate.subject.getField({shortName: 'OU'});
    that.subjectOrganizationalUnit = (field) ? field.value : '';
    field = forgeCertificate.subject.getField({name: 'organizationName'});
    that.subjectOrganization = (field) ? field.value : '';
    field = forgeCertificate.subject.getField({name: 'localityName'});
    that.subjectLocality = (field) ? field.value : '';
    field = forgeCertificate.subject.getField({name: 'countryName'});
    that.subjectCountry = (field) ? field.value : '';
}

function createIssuer(that, forgeCertificate) {
	let field = forgeCertificate.issuer.getField({name: 'commonName'});
	that.issuerCommonName = (field) ? field.value : '';
    field = forgeCertificate.issuer.getField({shortName: 'OU'});
    that.issuerOrganizationalUnit = (field) ? field.value : '';
    field = forgeCertificate.issuer.getField({name: 'organizationName'});
    that.issuerOrganization = (field) ? field.value : '';
    field = forgeCertificate.issuer.getField({name: 'localityName'});
    that.issuerLocality = (field) ? field.value : '';
    field = forgeCertificate.issuer.getField({name: 'countryName'});
    that.issuerCountry = (field) ? field.value : '';
}
