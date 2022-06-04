/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const KeyStoreService = require('./service');
const validator = require('../input-validator');

module.exports = {
    /**
     * Creates a new KeyStoreService object, which encapsulates a key store
     * service.
     *
     * @function newService
     * @global
     * @param {Object}
     *            [options] An object containing optional arguments.
     * @param {Policy}
     *            [options.policy=Default policy] The cryptographic policy to
     *            use.
     * @param {PbkdfRandomService}
     *            [options.pbkdfRandomService=Created internally] The PBKDF
     *            service to use.
     * @param {SymmetricCryptographyService}
     *            [options.symmetricCryptographyService=Created internally] The
     *            symmetric cryptography service to use.
     * @param {ElGamalCryptographyService}
     *            [options.elGamalCryptographyService=Created internally] The
     *            ElGamal cryptography service to use.
     * @returns {KeyStoreService} The new KeyStoreService object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    newService: function (options) {
        checkData(options);

        return new KeyStoreService(options);
    }
};

function checkData(options) {
    options = options || {};

    if (typeof options.policy !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.policy, 'Cryptographic policy provided to key store service');
    }

    if (typeof options.pbkdfService !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.pbkdfService,
            'PBKDF service object provided to key store service');
    }

    if (typeof options.symmetricCryptographyService !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.symmetricCryptographyService,
            'Symmetric cryptography service object provided to key store service');
    }

    if (typeof options.elGamalCryptographyService !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.elGamalCryptographyService,
            'ElGamal cryptography service object provided to key store service');
    }
}
