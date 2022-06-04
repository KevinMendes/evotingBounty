/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const constants = require('./constants');
const forge = require('node-forge');

module.exports = RsaPublicKey;

/**
 * @class RsaPublicKey
 * @classdesc Encapsulates an RSA public key. To instantiate this object, use
 *            the method {@link AsymmetricCryptographyService.newRsaPublicKey}.
 * @property {number} n The modulus.
 * @property {number} e The public exponent.
 */
function RsaPublicKey(params) {
	this.n = params.n;
	this.e = params.e;

	return Object.freeze(this);
}

RsaPublicKey.prototype = {
	/**
	 * Serializes this key into its PEM string representation.
	 *
	 * @function toPem
	 * @memberof RsaPublicKey
	 * @returns {string} The PEM string representation of this key.
	 */
	toPem: function () {
		const forgePublicKey = forge.pki.rsa.setPublicKey(this.n, this.e);

		return forge.pki.publicKeyToPem(forgePublicKey, constants.PEM_LINE_LENGTH);
	}
};
