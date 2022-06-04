/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const CertificateService = require('./service');

module.exports = {
    /**
     * Creates a new CertificateService object, which encapsulates a certificate
     * service.
     *
     * @function newService
     * @global
     * @returns {CertificateService} The new CertificateService object.
     */
    newService: function () {
        return new CertificateService();
    }
};
