/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const SecretKeyGenerator = require('./key-generator');
const SymmetricCipher = require('./cipher');
const MacHandler = require('./mac-handler');
const cryptoPolicy = require('../cryptopolicy');
const secureRandom = require('../securerandom');

module.exports = SymmetricCryptographyService;

/**
 * @class SymmetricCryptographyService
 * @classdesc The symmetric cryptography service API. To instantiate this
 *            object, use the method {@link newService}.
 * @hideconstructor
 * @param {Object}
 *            [options] An object containing optional arguments.
 * @param {Policy}
 *            [options.policy=Default policy] The cryptographic policy to use.
 * @param {SecureRandomService}
 *            [options.secureRandomService=Created internally] The secure random
 *            service to use.
 */
function SymmetricCryptographyService(options) {
	options = options || {};

	let _policy;
	if (options.policy) {
		_policy = options.policy;
	} else {
		_policy = cryptoPolicy.newInstance();
	}

	let _secureRandomService;
	if (options.secureRandomService) {
		_secureRandomService = options.secureRandomService;
	} else {
		_secureRandomService = secureRandom;
	}

	/**
	 * Creates a new SecretKeyGenerator object for generating secret keys.
	 *
	 * @function newKeyGenerator
	 * @memberof SymmetricCryptographyService
	 * @returns {SecretKeyGenerator} The new SecretKeyGenerator object.
	 */
	this.newKeyGenerator = function () {
		return new SecretKeyGenerator(_policy, _secureRandomService);
	};

	/**
	 * Creates a new SymmetricCipher object for symmetrically encrypting or
	 * decrypting data. It must be initialized with a secret key.
	 *
	 * @function newCipher
	 * @memberof SymmetricCryptographyService
	 * @returns {SymmetricCipher} The new SymmetricCipher object.
	 */
	this.newCipher = function () {
		return new SymmetricCipher(_policy, _secureRandomService);
	};

	/**
	 * Creates a new MacHandler object for generating or verifying a MAC. It
	 * must be initialized with a secret key.
	 *
	 * @function newMacHandler
	 * @memberof SymmetricCryptographyService
	 * @returns {MacHandler} The new MacHandler object.
	 */
	this.newMacHandler = function () {
		return new MacHandler(_policy);
	};
}
