/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const MathematicalService = require('./service');
const validator = require('../input-validator');

module.exports = {
    /**
     * Creates a new MathematicalService object, which encapsulates a
     * mathematical service.
     *
     * @function newService
     * @global
     * @param {Object}
     *            [options] An object containing optional arguments.
     * @param {Policy}
     *            [options.policy=Default policy] The cryptographic policy to
     *            use.
     * @param {SecureRandomService}
     *            [options.secureRandomService=Created internally] The secure
     *            random service to use.
     * @returns {MathematicalService} The new MathematicalService object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    newService: function (options) {
        checkData(options);

        return new MathematicalService(options);
    }
};

function checkData(options) {
    options = options || {};

    if (typeof options.policy !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.policy,
            'Cryptographic policy provided to zero-knowledge proof service');
    }

    if (typeof options.secureRandomService !== 'undefined') {
        validator.checkIsObjectWithProperties(
            options.secureRandomService,
            'Secure random service object provided to mathematical service');
    }
}
