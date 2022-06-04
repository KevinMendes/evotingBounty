/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const ElGamalPrivateKey = require('./private-key');
const mathematical = require('../mathematical');
const validator = require('../input-validator');
const codec = require('../codec');
const cryptoPolicy = require('../cryptopolicy');

module.exports = ElGamalCryptographyService;

/**
 * @class ElGamalCryptographyService
 * @classdesc The ElGamal cryptography service API. To instantiate this object,
 *            use the method {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 * @param {SecureRandomService}
 *            [options.secureRandomService=Created internally] The secure random
 *            service to use.
 * @param {MathematicalService}
 *            [options.mathematicalService=Created internally] The mathematical
 *            service to use.
 */
function ElGamalCryptographyService(options) {
    options = options || {};

    let policy;
    if (options.policy) {
        policy = options.policy;
    } else {
        policy = cryptoPolicy.newInstance();
    }

    let secureRandomService;
    if (options.secureRandomService) {
        secureRandomService = options.secureRandomService;
    }

    let _mathService;
    if (options.mathematicalService) {
        _mathService = options.mathematicalService;
    } else if (secureRandomService && policy) {
        _mathService =
            mathematical.newService({policy: policy, secureRandomService: secureRandomService});
    } else if (policy) {
        _mathService = mathematical.newService({policy: policy});
    } else {
        _mathService = mathematical.newService();
    }

    /**
     * Creates a new ElGamalPrivateKey object, which encapsulates an ElGamal
     * private key.
     *
     * @function newPrivateKey
     * @memberof ElGamalCryptographyService
     * @param {ZpSubgroup|string}
     *            groupOrJson The Zp subgroup to which the exponents of the
     *            private key are associated <b>OR</b> a JSON string
     *            representation of an ElGamalPrivateKey object, compatible with
     *            its <code>toJson</code> method. For the latter case, any
     *            additional input arguments will be ignored.
     * @param {Exponent[]}
     *            exponents The exponents that comprise the private key.
     * @returns {ElGamalPrivateKey} The new ElGamalPrivateKey object.
     * @throws {Error}
     *             If the input data validation fails.
     */
    this.newPrivateKey = function (groupOrJson, exponents) {
        if (typeof groupOrJson !== 'string') {
            validator.checkIsObjectWithProperties(
                groupOrJson, 'Zp subgroup for new ElGamalPrivateKey object');
            validator.checkExponents(
                exponents, 'Exponents for new ElGamalPrivateKey object',
                groupOrJson.q);
            _mathService.checkGroupMatchesPolicy(groupOrJson);

            return new ElGamalPrivateKey(groupOrJson, exponents);
        } else {
            return jsonToPrivateKey(groupOrJson);
        }
    };

    function jsonToPrivateKey(json) {
        validator.checkIsJsonString(
            json, 'JSON string representation of ElGamalPrivateKey object');

        const parsed = JSON.parse(json).privateKey;

        const g = codec.bytesToBigInteger(
            codec.base64Decode(parsed.zpSubgroup.g.toString()));
        const p = codec.bytesToBigInteger(
            codec.base64Decode(parsed.zpSubgroup.p.toString()));
        const q = codec.bytesToBigInteger(
            codec.base64Decode(parsed.zpSubgroup.q.toString()));
        const group = _mathService.newZpSubgroup(p, q, g);

        const exponents = [];
        for (let i = 0; i < parsed.exponents.length; i++) {
            const value =
                codec.bytesToBigInteger(codec.base64Decode(parsed.exponents[i]));
            const exponent = _mathService.newExponent(q, value);
            exponents.push(exponent);
        }

        return new ElGamalPrivateKey(group, exponents);
    }
}
