/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const CryptographicPolicy = require('./policy');
const options = require('./options');
const validator = require('../input-validator');

module.exports = {
    /**
     * Creates a new CryptographicPolicy object, which encapsulates a
     * cryptographic policy.
     *
     * <p>
     * By default, this object is created from the default cryptographic policy.
     * A customized policy may be provided instead, with the requirement that it
     * only uses options defined in {@link options}.
     *
     * @function newInstance
     * @global
     * @param {Object}
     *            [policy=Default policy] The cryptographic policy to use, as a
     *            JSON object.
     * @returns {CryptographicPolicy} The new CryptographicPolicy object.
     * @throws {Error} If the input data validation fails.
     */
    newInstance: function (policy) {
        if (typeof policy !== 'undefined') {
            validator.checkIsObjectWithProperties(policy, 'Cryptographic policy');
        }

        return new CryptographicPolicy(policy);
    },

    options: options
};
