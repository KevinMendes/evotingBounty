/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const constants = require('./constants');
const forge = require('node-forge');

module.exports = RsaPrivateKey;

/**
 * @class RsaPrivateKey
 * @classdesc Encapsulates an RSA private key. To instantiate this object, use
 *            the method {@link AsymmetricCryptographyService.newRsaPrivateKey}
 * @property {number} n The modulus.
 * @property {number} e The public exponent.
 * @property {number} d The private exponent.
 * @property {number} p The first prime.
 * @property {number} q The second prime.
 * @property {number} dP The first exponent.
 * @property {number} dQ The second exponent.
 * @property {number} qInv The coefficient.
 */
function RsaPrivateKey(params) {
    this.n = params.n;
    this.e = params.e;
    this.d = params.d;
    this.p = params.p;
    this.q = params.q;
    this.dP = params.dP;
    this.dQ = params.dQ;
    this.qInv = params.qInv;

    return Object.freeze(this);
}

RsaPrivateKey.prototype = {
    /**
     * Serializes this key into its PEM string representation.
     *
     * @function toPem
     * @memberof RsaPrivateKey
     * @returns {string} The PEM string representation of this key.
     */
    toPem: function () {
		const forgePrivateKey = forge.pki.rsa.setPrivateKey(
			this.n, this.e, this.d, this.p, this.q, this.dP, this.dQ, this.qInv);

		return forge.pki.privateKeyToPem(
            forgePrivateKey, constants.PEM_LINE_LENGTH);
    }
};
