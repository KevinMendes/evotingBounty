/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const SymmetricCryptographyService = require('./service');
const validator = require('../input-validator');

module.exports = {
	/**
	 * Creates a new SymmetricCryptographyService object, which encapsulates a
	 * symmetric cryptography service.
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
	 * @returns {SymmetricCryptographyService} The new
	 *          SymmetricCryptographyService object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	newService: function (options) {
		checkData(options);

		return new SymmetricCryptographyService(options);
	}
};

function checkData(options) {
	options = options || {};

	if (typeof options.policy !== 'undefined') {
		validator.checkIsObjectWithProperties(
			options.policy,
			'Cryptographic policy provided to symmetric cryptography service');
	}

	if (typeof options.secureRandomService !== 'undefined') {
		validator.checkIsObjectWithProperties(
			options.secureRandomService,
			'Secure random service object provided to symmetric cryptography service');
	}
}
