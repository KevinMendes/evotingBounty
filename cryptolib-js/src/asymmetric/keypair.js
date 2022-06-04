/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

module.exports = KeyPair;

/**
 * @class KeyPair
 * @classdesc Encapsulates a key pair. To instantiate this object, use the
 *            method {@link AsymmetricCryptographyService.newKeyPair}.
 * @property {string} publicKey The public key, in PEM format.
 * @property {string} privateKey The private key, in PEM format.
 */
function KeyPair(publicKey, privateKey) {
	return Object.freeze({publicKey: publicKey, privateKey: privateKey});
}
