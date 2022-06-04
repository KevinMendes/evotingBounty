/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const AsymmetricCryptographyService = require('./service');
const validator = require('../input-validator');

module.exports = {
	/**
	 * Creates a new AsymmetricCryptographyService object, which encapsulates an
	 * asymmetric cryptography service.
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
	 * @returns {AsymmetricCryptographyService} The new
	 *          AsymmetricCryptographyService object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	newService: function (options) {
		checkData(options);

		return new AsymmetricCryptographyService(options);
	}
};

function checkData(options) {
	options = options || {};

	if (typeof options.policy !== 'undefined') {
		validator.checkCryptographicPolicy(
			options.policy,
			'Cryptographic policy provided to asymmetric cryptography service');
	}

	if (typeof options.secureRandomService !== 'undefined') {
		validator.checkSecureRandomService(
			options.secureRandomService,
			'Secure random service object provided to asymmetric cryptography service');
	}
}
