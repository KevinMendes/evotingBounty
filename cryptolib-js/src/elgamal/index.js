/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const ElGamalCryptographyService = require('./service');
const validator = require('../input-validator');

module.exports = {
	/**
	 * Creates a new ElGamalCryptographyService object, which encapsulates an
	 * ElGamal cryptography service.
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
	 * @param {MathematicalService}
	 *            [options.mathematicalService=Created internally] The
	 *            mathematical service to use.
	 * @returns {ElGamalCryptographyService} The new ElGamalCryptographyService
	 *          object.
	 * @throws {Error}
	 *             If the input data validation fails.
	 */
	newService: function (options) {
		checkData(options);

		return new ElGamalCryptographyService(options);
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
			'Secure random service object provided to ElGamal cryptography service');
	}

	if (typeof options.mathematicalService !== 'undefined') {
		validator.checkIsObjectWithProperties(
			options.mathematicalService,
			'Mathematical service object provided to ElGamal cryptography service');
	}
}
