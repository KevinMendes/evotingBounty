/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

/* jshint node:true */
'use strict';

const SecureRandomGenerator = require('./generator');
const SecureRandomService = require('./service');

module.exports = {
	/**
	 * Creates a new SecureRandomGenerator object.
	 * This object can be used for all supported types of random generation.
	 *
	 * @function newRandomGenerator
	 * @memberof SecureRandomService
	 * @param {Object}
	 *            [options] An object containing optional arguments.
	 * @param {number}
	 *            [options.maxNumBytes=512] The maximum number of bytes that can
	 *            randomly generated per method call.
	 * @param {number}
	 *            [options.maxNumDigits=512] The maximum number of BigInteger
	 *            digits that can randomly generated per method call.
	 * @returns {SecureRandomGenerator} The new SecureRandomGenerator object.
	 */
	newRandomGenerator: function (options) {
		return new SecureRandomGenerator(options);
	},

	/**
	 * Creates a new SecureRandomService object, which encapsulates a secure
	 * random service.
	 *
	 * @function newService
	 * @global
	 * @returns {SecureRandomService} The new SecureRandomService object.
	 */
	newService: function () {
		return new SecureRandomService();
	}
};
