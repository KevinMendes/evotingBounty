/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const X509Certificate = require('./x509-certificate');
const CertificateValidator = require('./certificate-validator');
const validator = require('../input-validator');

module.exports = CertificateService;

/**
 * @class CertificateService
 * @classdesc The certificate service API. To instantiate this object, use the
 *            method {@link newService}.
 * @hideconstructor
 */
function CertificateService() {
    // Empty constructor
}

CertificateService.prototype = {
    /**
     * Creates a new X509Certificate object and loads it with the provided X.509
     * certificate.
     *
     * @function newX509Certificate
     * @memberof CertificateService
     * @param {string}
     *            certificate The provided X.509 certificate, in PEM format.
     * @returns {X509Certificate} The X509Certificate object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    newX509Certificate: function (certificate) {
        validator.checkIsNonEmptyString(
            certificate, 'X.509 certificate in PEM format');

        return new X509Certificate(certificate);
    },

    /**
     * Creates a new CertificateValidator object for validating individual
     * certificates or certificate chains.
     *
     * @function newValidator
     * @memberof CertificateService
     * @returns {CertificateValidator} The CertificateValidator object.
     */
    newValidator: function () {
        return new CertificateValidator();
    }
};
